package guru.zoroark.shedinja.dsl

import guru.zoroark.shedinja.ShedinjaException
import guru.zoroark.shedinja.environment.Declaration
import guru.zoroark.shedinja.environment.EnvironmentContext

/**
 * Builder DSL for creating an environment.This part of the DSL is specifically responsible for creating an
 * [EnvironmentContext].
 */
@ShedinjaDsl
class EnvironmentContextBuilderDsl : Buildable<EnvironmentContext>, ContextBuilderDsl {
    private val declaredComponents = mutableListOf<Declaration<*>>()

    @ShedinjaDsl
    override fun <T : Any> put(declaration: Declaration<T>) {
        if (declaredComponents.any { it.identifier == declaration.identifier }) {
            throw ShedinjaException(
                "Duplicate identifier: Tried to put '${declaration.identifier}', but one was already present"
            )
        }
        declaredComponents.add(declaration)
    }

    override fun build(): BuildResult<EnvironmentContext> {
        val results = declaredComponents.associateBy { it.identifier }
        return BuildResult.Success(EnvironmentContext(results))
    }
}
