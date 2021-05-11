package guru.zoroark.shedinja.dsl

import guru.zoroark.shedinja.ShedinjaException
import guru.zoroark.shedinja.environment.Declaration
import guru.zoroark.shedinja.environment.EnvironmentContext
import guru.zoroark.shedinja.environment.Identifier
import guru.zoroark.shedinja.environment.ScopedSupplier
import kotlin.reflect.KClass

/**
 * Builder DSL for creating an environment.This part of the DSL is specifically responsible for creating an
 * [EnvironmentContext].
 */
@ShedinjaDsl
class EnvironmentBuilderDsl : Buildable<EnvironmentContext> {
    private val declaredComponents = mutableListOf<DeclarationBuilder<*>>()

    /**
     * Adds an object to this builder of type [kclass] that will be built using the given [supplier].
     *
     * @param kclass The class of the object
     * @param supplier The supplier of the object
     */
    @ShedinjaDsl
    fun <T : Any> put(kclass: KClass<T>, supplier: ScopedSupplier<T>) {
        if (declaredComponents.any { it.kclass == kclass }) {
            throw ShedinjaException(
                "Duplicate identifier: Tried to put '${kclass.qualifiedName}', but one was already present"
            )
        }
        declaredComponents.add(DeclarationBuilder(kclass, supplier))
    }

    override fun build(): BuildResult<EnvironmentContext> {
        val results = declaredComponents
            .map { TypelessDeclaration(Identifier(it.kclass), it.supplier) }
            .associateBy { it.identifier }
        return BuildResult.Success(EnvironmentContext(results))
    }
}

@Suppress("FunctionName", "UNCHECKED_CAST")
private fun TypelessDeclaration(identifier: Identifier<*>, supplier: ScopedSupplier<*>): Declaration<*> {
    fun <T : Any> fakeTypedDeclaration(): Declaration<*> {
        // For whatever reason, using just <*> in both cases leads to a compilation error:
        // Type mismatch: inferred type is Any but CapturedType(*) was expected
        return Declaration(identifier as Identifier<T>, supplier as ScopedSupplier<T>)
    }
    return fakeTypedDeclaration<Any>()
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
inline fun <reified T : Any> EnvironmentBuilderDsl.put(noinline supplier: ScopedSupplier<T>) {
    put(T::class, supplier)
}
