package guru.zoroark.shedinja.dsl

import guru.zoroark.shedinja.environment.Declaration
import guru.zoroark.shedinja.environment.InjectableModule

/**
 * DSL builder class for [InjectableModule]s. It does not add functionality other than the building logic. All DSL
 * functionalities are provided as extension functions of [ContextBuilderDsl].
 */
@ShedinjaDsl
class ModuleBuilderDsl(private val name: String) : ContextBuilderDsl, Buildable<InjectableModule> {
    private val declarations = mutableListOf<Declaration<*>>()
    override fun <T : Any> put(declaration: Declaration<T>) {
        declarations += declaration
    }

    override fun build(): InjectableModule =
        InjectableModule(name, declarations)
}
