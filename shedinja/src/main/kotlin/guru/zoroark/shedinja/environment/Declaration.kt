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
 * A map that maps identifiers to declarations.
 *
 * The general contract is:
 *
 * - For any key-value pair, key == value.identifier
 * - For any key-value pair, key: Identifier<T> => value: Declaration<T>
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
