package guru.zoroark.shedinja.environment

/**
 * A declaration within an [EnvironmentContext].
 *
 * A declaration is an [Identifier] coupled with a [scoped supplier][ScopedSupplier]. Declarations can be eagerly or
 * lazily invoked depending on the underlying environment's general contract.
 *
 * @property identifier The identifier for this declaration.
 * @property supplier The supplier for this declaration.
 */
class Declaration<T : Any>(val identifier: Identifier<T>, val supplier: ScopedSupplier<T>)

/**
 * Creates a declaration from the given identifier and supplier. Only use this function if you do not have type
 * information on both the identifier and the supplier. Any other use is heavily discouraged.
 */
// TODO document param and return
@Suppress("FunctionName", "UNCHECKED_CAST")
fun TypelessDeclaration(identifier: Identifier<*>, supplier: ScopedSupplier<*>): Declaration<*> {
    fun <T : Any> fakeTypedDeclaration(): Declaration<*> {
        // For whatever reason, using just <*> in both cases leads to a compilation error:
        // Type mismatch: inferred type is Any but CapturedType(*) was expected
        return Declaration(identifier as Identifier<T>, supplier as ScopedSupplier<T>)
    }
    return fakeTypedDeclaration<Any>()
}

/**
 * A map that maps identifiers to declarations.
 *
 * The general contract is:
 *
 * - For any key-value pair, key == value.identifier
 * - For any key-value pair, key: Identifier of T => value: Declaration of T
 */
typealias Declarations = Map<Identifier<*>, Declaration<*>>

/**
 * Retrieves the declaration that corresponds to the given type parameter. This function does *not* check for the
 * validity or coherence of the returned declaration.
 */
inline fun <reified T : Any> Declarations.get(): Declaration<T> {
    val found = this[Identifier(T::class)] ?: error("Type not found: ${T::class.qualifiedName ?: "(anonymous)"}")
    @Suppress("UNCHECKED_CAST")
    return found as Declaration<T>
}
