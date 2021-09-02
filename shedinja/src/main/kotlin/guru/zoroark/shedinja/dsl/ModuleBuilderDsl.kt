package guru.zoroark.shedinja.dsl

import guru.zoroark.shedinja.environment.Declaration
import guru.zoroark.shedinja.environment.InjectableModule

@ShedinjaDsl
class ModuleBuilderDsl(private val name: String) : ContextBuilderDsl, Buildable<InjectableModule> {
    private val declarations = mutableListOf<Declaration<*>>()
    override fun <T : Any> put(declaration: Declaration<T>) {
        declarations += declaration
    }

    override fun build(): BuildResult<InjectableModule> =
        BuildResult.Success(InjectableModule(name, declarations))
}
