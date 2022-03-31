package guru.zoroark.shedinja.extensions.services

import guru.zoroark.shedinja.ExtensionNotInstalledException
import guru.zoroark.shedinja.dsl.ShedinjaDsl
import guru.zoroark.shedinja.dsl.put
import guru.zoroark.shedinja.environment.Declaration
import guru.zoroark.shedinja.environment.Identifier
import guru.zoroark.shedinja.environment.InjectionScope
import guru.zoroark.shedinja.environment.getOrNull
import guru.zoroark.shedinja.environment.invoke
import guru.zoroark.shedinja.extensions.DeclarationsProcessor
import guru.zoroark.shedinja.extensions.ExtensibleContextBuilderDsl
import guru.zoroark.shedinja.extensions.ExtensibleInjectionEnvironment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.util.Locale
import kotlin.coroutines.cancellation.CancellationException
import kotlin.reflect.full.isSubclassOf
import kotlin.system.measureTimeMillis

private enum class OperationType(val confirmationWord: String, val ingWord: String) {
    Start("started", "starting"),
    Stop("stopped", "stopping")
}

private fun OperationType.isBlockedByPolicy(policy: IgnorePolicy?): Boolean {
    return when (policy) {
        null -> false
        IgnorePolicy.IgnoreAll -> true
        IgnorePolicy.IgnoreStart -> this == OperationType.Start
        IgnorePolicy.IgnoreStop -> this == OperationType.Stop
    }
}

/**
 * Exceptions that occur within the starting or stopping process are wrapped with this type.
 */
class ShedinjaServiceException(message: String, cause: Throwable) : Exception(message, cause)

/**
 * Class for the [services extension][useServices] logic.
 *
 * This class is injected in extensible environments' meta-environment. You can retrieve this class by using
 * [services] on an extensible environment.
 */
class ServiceManager(scope: InjectionScope) : DeclarationsProcessor {
    private val environment: ExtensibleInjectionEnvironment by scope()
    private val ignorePolicies = mutableMapOf<Identifier<*>, IgnorePolicy>()

    private fun getServices(operationType: OperationType): Sequence<Pair<Identifier<*>, ShedinjaService>> =
        environment.getAllIdentifiers()
            .filter { it.kclass.isSubclassOf(ShedinjaService::class) }
            .filterNot { operationType.isBlockedByPolicy(ignorePolicies[it]) }
            .map {
                @Suppress("UNCHECKED_CAST")
                it to environment.get(it as Identifier<ShedinjaService>)
            }

    private fun getSuspendedServices(
        operationType: OperationType
    ): Sequence<Pair<Identifier<*>, SuspendShedinjaService>> =
        environment.getAllIdentifiers()
            .filter { it.kclass.isSubclassOf(SuspendShedinjaService::class) }
            .filterNot { operationType.isBlockedByPolicy(ignorePolicies[it]) }
            .map {
                @Suppress("UNCHECKED_CAST")
                it to environment.get(it as Identifier<SuspendShedinjaService>)
            }

    /**
     * Starts all the [ShedinjaService] and [SuspendShedinjaService] components registered in this environment.
     *
     * Services [tagged][guru.zoroark.shedinja.extensions.DeclarationTag] with [noService]/[IgnorePolicy.IgnoreAll] or
     * [noServiceStart]/[IgnorePolicy.IgnoreStart] are ignored and do not get started when calling this function.
     *
     * This function runs blocking code (i.e. [ShedinjaService.start]) within the [Dispatchers.IO] dispatcher, and runs
     * suspending functions (i.e. [SuspendShedinjaService.start]) asynchronously within the current context.
     */
    suspend fun startAll(
        messageHandler: (String) -> Unit = { /* no-op */ }
    ): Map<Identifier<*>, Long> =
        doForEachDeclaration(
            OperationType.Start,
            messageHandler,
            { it.start() },
            { it.start() }
        )

    /**
     * Stops all the [ShedinjaService] and [SuspendShedinjaService] components registered in this environment.
     *
     * Services [tagged][guru.zoroark.shedinja.extensions.DeclarationTag] with [noService]/[IgnorePolicy.IgnoreAll] or
     * [noServiceStop]/[IgnorePolicy.IgnoreStop] are ignored and do not get started when calling this function.
     *
     * This function runs blocking code (i.e. [ShedinjaService.stop]) within the [Dispatchers.IO] dispatcher, and runs
     * suspending functions (i.e. [SuspendShedinjaService.stop]) asynchronously within the current context.
     */
    suspend fun stopAll(
        messageHandler: (String) -> Unit = { /* no-op */ }
    ): Map<Identifier<*>, Long> =
        doForEachDeclaration(
            OperationType.Stop,
            messageHandler,
            { it.stop() },
            { it.stop() }
        )

    override fun processDeclarations(sequence: Sequence<Declaration<*>>) {
        sequence.forEach { declaration ->
            declaration.tags
                .filterIsInstance<IgnorePolicy>()
                // Combine all policies into one, see the + operator definition.
                .fold<IgnorePolicy, IgnorePolicy?>(null) { initial, next ->
                    (initial ?: next) + next
                }
                ?.let { ignorePolicies[declaration.identifier] = it }
        }
    }

    private suspend fun doForEachDeclaration(
        operationType: OperationType,
        messageHandler: (String) -> Unit,
        onNonSuspend: suspend (ShedinjaService) -> Unit,
        onSuspsend: suspend (SuspendShedinjaService) -> Unit
    ): Map<Identifier<*>, Long> = coroutineScope {
        val toAwait = getSuspendedServices(operationType).map { (identifier, service) ->
            async {
                catching(operationType, identifier) {
                    val timeTaken = measureTimeMillis { onSuspsend(service) }
                    messageHandler("Service $identifier ${operationType.confirmationWord} in $timeTaken ms")
                    identifier to timeTaken
                }
            }
        }.toList() + getServices(operationType).map { (identifier, service) ->
            async(Dispatchers.IO) {
                catching(operationType, identifier) {
                    val timeTaken = measureTimeMillis { onNonSuspend(service) }
                    messageHandler("Service $identifier ${operationType.confirmationWord} in $timeTaken ms")
                    identifier to timeTaken
                }
            }
        }.toList()
        val result = toAwait.awaitAll()
        result.associateBy({ it.first }) { it.second }
    }

    @Suppress("TooGenericExceptionCaught") // Kind of the entire point here
    private inline fun <T> catching(operationType: OperationType, identifier: Identifier<*>, block: () -> T): T {
        try {
            return block()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            throw ShedinjaServiceException(
                "${operationType.ingWord.capitalize()} service $identifier failed", e
            )
        }
    }

    private fun String.capitalize() =
        replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() }
}

/**
 * Installs the Services extension onto this extensible environment.
 *
 * This extension gives you the ability to start and stop *services* within your environment using the
 * [ServiceManager.startAll] and [ServiceManager.stopAll] functions.
 *
 * ### Creating services
 *
 * A service is a component that implements either [ShedinjaService] or [SuspendShedinjaService]. Note that services
 * should not (and generally cannot due to compiler limitations) implement both interfaces.
 *
 * ### Starting and stopping services
 *
 * You can retrieve a [ServiceManager] instance from the environment after having installed this extension by using
 * [`env.services`][services] (where env is the environment). You can in turn use the service manager's
 * [startAll][ServiceManager.startAll] and [stopAll][ServiceManager.stopAll] functions to start and stop all the
 * services.
 *
 * ### Excluding services
 *
 * Services can be excluded from being started or stopped by tagging their declaration with the appropriate tag:
 *
 * - To fully ignore a service, use [noService]
 * - To ignore a service when starting, but not ignore when stopping, use [noServiceStart]
 * - To ignore a service when stopping, but not ignore when starting, use [noServiceStop]
 */
@ShedinjaDsl
fun ExtensibleContextBuilderDsl.useServices() {
    meta { put(::ServiceManager) }
}

/**
 * Retrieves the [ServiceManager] that was created when running [useServices] while building the environment.
 *
 * Throws an exception if the extension is not currently installed.
 */
val ExtensibleInjectionEnvironment.services: ServiceManager
    get() = metaEnvironment.getOrNull()
        ?: throw ExtensionNotInstalledException(
            "Services extension is not installed. Install the service manager by adding 'useServices()' in your " +
                "'shedinja' block."
        )
