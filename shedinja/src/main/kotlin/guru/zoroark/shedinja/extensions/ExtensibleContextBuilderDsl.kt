package guru.zoroark.shedinja.extensions

import guru.zoroark.shedinja.dsl.BuildResult
import guru.zoroark.shedinja.dsl.Buildable
import guru.zoroark.shedinja.dsl.ContextBuilderDsl
import guru.zoroark.shedinja.dsl.EnvironmentContextBuilderDsl
import guru.zoroark.shedinja.dsl.ShedinjaDsl
import guru.zoroark.shedinja.dsl.getOrThrow
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

    override fun build(): BuildResult<ExtensibleEnvironmentContext> {
        val regular = regularContextBuilder.build()
        if (regular is BuildResult.Failure) {
            return BuildResult.Failure(regular.errors, regular.warnings)
        }
        val meta = metaContextBuilder.build()
        return if (meta is BuildResult.Failure) BuildResult.Failure(meta.errors, meta.warnings)
        else BuildResult.Success(ExtensibleEnvironmentContext(regular.getOrThrow().declarations, meta.getOrThrow()))
    }
}
