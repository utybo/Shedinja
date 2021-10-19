package guru.zoroark.shedinja.extensions.services

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
import kotlin.reflect.full.isSubclassOf

private enum class OperationType {
    Start,
    Stop
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
 * Class for the [services extension][useServices] logic.
 *
 * This class is injected in extensible environments' meta-environment. You can retrieve this class by using
 * [services] on an extensible environment.
 */
class ServiceManager(scope: InjectionScope) : DeclarationsProcessor {
    private val environment: ExtensibleInjectionEnvironment by scope()
    private val ignorePolicies = mutableMapOf<Identifier<*>, IgnorePolicy>()

    private fun getServices(operationType: OperationType): Sequence<ShedinjaService> =
        environment.getAllIdentifiers()
            .filter { it.kclass.isSubclassOf(ShedinjaService::class) }
            .filterNot { operationType.isBlockedByPolicy(ignorePolicies[it]) }
            .map {
                @Suppress("UNCHECKED_CAST")
                environment.get(it as Identifier<ShedinjaService>)
            }

    private fun getSuspendedServices(operationType: OperationType): Sequence<SuspendShedinjaService> =
        environment.getAllIdentifiers()
            .filter { it.kclass.isSubclassOf(SuspendShedinjaService::class) }
            .filterNot { operationType.isBlockedByPolicy(ignorePolicies[it]) }
            .map {
                @Suppress("UNCHECKED_CAST")
                environment.get(it as Identifier<SuspendShedinjaService>)
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
    suspend fun startAll(): Unit = coroutineScope {
        val toAwait = getSuspendedServices(OperationType.Start).map {
            async { it.start() }
        }.toList() + getServices(OperationType.Start).map {
            // Offload blocking operation to the IO dispatcher, which is built for that.
            async(Dispatchers.IO) { it.start() }
        }.toList()
        toAwait.awaitAll()
    }

    /**
     * Stops all the [ShedinjaService] and [SuspendShedinjaService] components registered in this environment.
     *
     * Services [tagged][guru.zoroark.shedinja.extensions.DeclarationTag] with [noService]/[IgnorePolicy.IgnoreAll] or
     * [noServiceStop]/[IgnorePolicy.IgnoreStop] are ignored and do not get started when calling this function.
     *
     * This function runs blocking code (i.e. [ShedinjaService.stop]) within the [Dispatchers.IO] dispatcher, and runs
     * suspending functions (i.e. [SuspendShedinjaService.stop]) asynchronously within the current context.
     */
    suspend fun stopAll(): Unit = coroutineScope {
        val toAwait = getSuspendedServices(OperationType.Stop).map {
            async { it.stop() }
        }.toList() + getServices(OperationType.Stop).map {
            async(Dispatchers.IO) { it.stop() }
        }.toList()
        toAwait.awaitAll()
    }

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
        ?: error(
            "Services extension is not installed. Install the service manager by adding 'useServices()' in your " +
                    "'shedinja' block."
        )
