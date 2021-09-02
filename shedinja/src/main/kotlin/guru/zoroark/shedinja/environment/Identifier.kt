package guru.zoroark.shedinja.environment

import kotlin.reflect.KClass

/**
 * Identifies an injectable component via its type and optionally via other elements.
 *
 * @property kclass The class this identifier wraps
 */
data class Identifier<T : Any>(val kclass: KClass<T>) {
    override fun toString(): String {
        return kclass.qualifiedName ?: "<anonymous>"
    }
}
