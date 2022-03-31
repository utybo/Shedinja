package guru.zoroark.shedinja.extensions

import guru.zoroark.shedinja.dsl.Buildable
import guru.zoroark.shedinja.dsl.ContextBuilderDsl
import guru.zoroark.shedinja.dsl.EnvironmentContextBuilderDsl
import guru.zoroark.shedinja.dsl.ShedinjaDsl
import guru.zoroark.shedinja.environment.Declaration

/**
 * A context builder which can also receive meta-environment components via the [meta] function.
 */
@ShedinjaDsl
interface ExtensibleContextBuilderDsl : ContextBuilderDsl {
    /**
     * Executes the given lambda (which takes a [ContextBuilderDsl]) to execute actions on the
     * meta-environment.
     *
     * For example, you can add a component to the meta-environment like so:
     *
     * ```
     * meta {
     *     put(::MyComponent)
     * }
     * ```
     */
    @ShedinjaDsl
    fun meta(action: ContextBuilderDsl.() -> Unit)
}

/**
 * Default builder for extensible environment contexts using the DSL.
 */
@ShedinjaDsl
class ExtensibleEnvironmentContextBuilderDsl : ExtensibleContextBuilderDsl, Buildable<ExtensibleEnvironmentContext> {
    private val regularContextBuilder = EnvironmentContextBuilderDsl()
    private val metaContextBuilder = EnvironmentContextBuilderDsl()

    override fun meta(action: ContextBuilderDsl.() -> Unit) {
        action(metaContextBuilder)
    }

    override fun <T : Any> put(declaration: Declaration<T>) {
        regularContextBuilder.put(declaration)
    }

    override fun build(): ExtensibleEnvironmentContext {
        val regular = regularContextBuilder.build()
        val meta = metaContextBuilder.build()
        return ExtensibleEnvironmentContext(regular.declarations, meta)
    }
}
