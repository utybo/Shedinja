package guru.zoroark.shedinja.extensions

import guru.zoroark.shedinja.environment.Declaration
import guru.zoroark.shedinja.environment.EnvironmentContext
import guru.zoroark.shedinja.environment.Identifier
import guru.zoroark.shedinja.environment.InjectionEnvironment
import guru.zoroark.shedinja.environment.InjectionEnvironmentKind
import kotlin.reflect.full.isSubclassOf

/**
 * An extensible injection environment is an injection environment that supports extensions.
 *
 * While some additional features (such as [factories][guru.zoroark.shedinja.extensions.factory.putFactory]) can be
 * implemented purely using Shedinja's public API, some extensions simply cannot do this and require:
 *
 * - Analysis of declarations outside of tests.
 * - State storage when just putting more stuff within the environment is not feasible.
 *
 * ### Meta-environments
 *
 * Extensible environments are, at their core, environments with an additional sub-environment named the "meta
 * environment". By default, meta-environments are [EagerImmutableMetaEnvironment] objects. This meta environment can be
 * used to:
 *
 * - Store additional states (via regular Shedinja [put][guru.zoroark.shedinja.dsl.put]/[get][InjectionEnvironment.get]/
 * [inject][InjectionEnvironment.createInjector] mechanisms).
 * - [Process declarations][DeclarationsProcessor] that were put in the regular environment.
 *
 * You should not implement this interface directly yourself: subclass [DefaultExtensibleInjectionEnvironment] instead,
 * which already handles all the extensions-related logic.
 *
 * ### Conventions
 *
 * Extensible injection environments should follow the same conventions as regular injection environments, especially
 * for documentation and usage. However, you **must not** use [InjectionEnvironmentKind] for extension environments --
 * otherwise, the DSL will not see your environment as extensible. Use [ExtensibleInjectionEnvironmentKind] instead.
 *
 * In order to uniformly document extensibility, implementors should include the following line in their KDoc comment:
 *
 * > Compatible with installable extensions.
 */
interface ExtensibleInjectionEnvironment : InjectionEnvironment {
    /**
     * The meta environment for this environment.
     *
     * The meta environment *must not* be extensible itself.
     */
    val metaEnvironment: InjectionEnvironment

    /**
     * Returns a sequence of all the known identifiers present in this environment.
     */
    fun getAllIdentifiers(): Sequence<Identifier<*>>
}

/**
 * Default implementation for [ExtensibleInjectionEnvironment].
 */
abstract class DefaultExtensibleInjectionEnvironment(
    context: ExtensibleEnvironmentContext,
    metaContextKind: InjectionEnvironmentKind<*> = EagerImmutableMetaEnvironment
) : ExtensibleInjectionEnvironment {
    override val metaEnvironment = metaContextKind.build(context.metaContext.let {
        // Inject the EIE within the meta-environment
        val declaration = Declaration(Identifier(ExtensibleInjectionEnvironment::class)) {
            this@DefaultExtensibleInjectionEnvironment
        }
        val newDeclarations = it.declarations.toMutableMap()
        newDeclarations[declaration.identifier] = declaration
        EnvironmentContext(newDeclarations)
    }).also { metaEnvironment ->
        context.metaContext.declarations.keys.filter {
            it.kclass.isSubclassOf(DeclarationsProcessor::class)
        }.forEach { processorIdentifier ->
            val processor = metaEnvironment.get(processorIdentifier) as? DeclarationsProcessor
                ?: error(
                    "Internal error: processor has an identifier declaring it is a subclass of " +
                            "DeclarationsProcessor, but the actual object is not."
                )
            processor.processDeclarations(context.declarations.values.asSequence())
        }
    }
}
