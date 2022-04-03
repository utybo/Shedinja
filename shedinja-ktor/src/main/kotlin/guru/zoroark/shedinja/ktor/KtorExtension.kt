package guru.zoroark.shedinja.ktor

import guru.zoroark.shedinja.dsl.ShedinjaDsl
import guru.zoroark.shedinja.dsl.put
import guru.zoroark.shedinja.environment.Declaration
import guru.zoroark.shedinja.environment.Identifier
import guru.zoroark.shedinja.environment.InjectionScope
import guru.zoroark.shedinja.environment.invoke
import guru.zoroark.shedinja.extensions.DeclarationsProcessor
import guru.zoroark.shedinja.extensions.ExtensibleContextBuilderDsl
import guru.zoroark.shedinja.extensions.ExtensibleInjectionEnvironment
import kotlin.reflect.full.isSubclassOf

/**
 * The Ktor extension object that is injected into the meta-environment. Keeps track of implementations of
 * [KtorModule] subclasses (incl. [KtorController] subclasses) within the main environment.
 */
class KtorExtension(scope: InjectionScope) : DeclarationsProcessor {
    private val environment: ExtensibleInjectionEnvironment by scope()

    /**
     * Returns modules available in the environment for the given application name.
     */
    fun getModulesForAppName(appName: String?): List<KtorModule> {
        return modulesIdentifiers
            .map { environment.get(it) }
            .filterIsInstance<KtorModule>()
            .filter { it.restrictToAppName == appName }
            .sortedByDescending { it.moduleInstallationPriority }
    }

    private val modulesIdentifiers = mutableListOf<Identifier<*>>()

    override fun processDeclarations(sequence: Sequence<Declaration<*>>) {
        sequence
            .filter { it.identifier.kclass.isSubclassOf(KtorModule::class) }
            .forEach { modulesIdentifiers += it.identifier }
    }
}

/**
 * Adds the Ktor extension to this environment. Note that you will also need to add the services extension in order to
 * use the Ktor extension with `useServices()`.
 */
@ShedinjaDsl
fun ExtensibleContextBuilderDsl.useKtor() {
    meta { put(::KtorExtension) }
}
