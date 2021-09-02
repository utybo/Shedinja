package guru.zoroark.shedinja.dsl

import guru.zoroark.shedinja.environment.Declaration
import guru.zoroark.shedinja.environment.Identifier
import guru.zoroark.shedinja.environment.InjectableModule
import guru.zoroark.shedinja.environment.SComponent
import guru.zoroark.shedinja.environment.ScopedSupplier
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.createType

@ShedinjaDsl
interface ContextBuilderDsl {
    @ShedinjaDsl
    fun <T : Any> put(declaration: Declaration<T>)
}

/**
 * Add a definition of type [T] with the given supplier.
 *
 * [T] is internally turned to a KClass.
 *
 * @param supplier The lambda that will be executed to create an object
 * @param T The type of the component to add
 */
@ShedinjaDsl
inline fun <reified T : Any> ContextBuilderDsl.put(noinline supplier: ScopedSupplier<T>) {
    put(T::class, supplier)
}

@ShedinjaDsl
inline fun <reified T : Any> ContextBuilderDsl.put(supplier: KFunction<T>) {
    when {
        supplier.returnType.isMarkedNullable ->
            error("Cannot 'put' a function that has a nullable return type.")
        supplier.parameters.isEmpty() ->
            put { supplier.call() }
        // Is single scope?
        supplier.parameters.size == 1 && supplier.parameters.first().type == SComponent::class.createType() ->
            put { supplier.call(scope) }
        else ->
            error("Cannot 'put' the given function ($supplier). It must take either no arguments or a single argument that is of type 'SComponent'.")
    }
}

@ShedinjaDsl
fun <T : Any> ContextBuilderDsl.put(kclass: KClass<T>, supplier: ScopedSupplier<T>) {
    put(Declaration(Identifier(kclass), supplier))
}

@ShedinjaDsl
fun ContextBuilderDsl.put(supplier: InjectableModule) {
    supplier.declarations.forEach { put(it) }
}
