package guru.zoroark.shedinja.environment

import kotlin.reflect.KClass

/**
 * Identifies a injectable component via its type and optionally via other elements.
 *
 * @property kclass Thee class this identifier wraps
 */
data class Identifier<T : Any>(val kclass: KClass<T>)
