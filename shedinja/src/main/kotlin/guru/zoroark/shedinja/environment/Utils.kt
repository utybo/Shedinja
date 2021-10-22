package guru.zoroark.shedinja.environment

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Utility function that wraps a given property using the given wrapper. This is useful when you want to transform the
 * output of a property in some way.
 *
 * This caches the result -- the `mapper` is executed lazily the first time the property is `get`ed.
 */
inline infix fun <T, V, R : Any> ReadOnlyProperty<T, V>.wrapIn(
    crossinline mapper: (V) -> R
): SynchronizedLazyPropertyWrapper<T, R> =
    SynchronizedLazyPropertyWrapper(WrappedReadOnlyProperty(this, mapper))

/**
 * Wraps a property and maps its result using the given mapper.
 */
@Suppress("FunctionName")
inline fun <T, V, R> WrappedReadOnlyProperty(
    original: ReadOnlyProperty<T, V>,
    crossinline mapper: (V) -> R
): ReadOnlyProperty<T, R> =
    ReadOnlyProperty { thisRef, property ->
        mapper(original.getValue(thisRef, property))
    }

/**
 * Similar to `lazy { }` but uses a property instead of a lambda for building. Inspired by the `SYNCHRONIZED` lazy
 * implementation.
 */
class SynchronizedLazyPropertyWrapper<T, V : Any>(private val wrappedProperty: ReadOnlyProperty<T, V>) :
    ReadOnlyProperty<T, V> {
    @Volatile
    private var value: V? = null

    override fun getValue(thisRef: T, property: KProperty<*>): V {
        val valueNow = value
        if (valueNow != null) {
            return valueNow
        }

        return synchronized(this) {
            val valueNow2 = value
            if (valueNow2 != null) {
                valueNow2
            } else {
                val newValue = wrappedProperty.getValue(thisRef, property)
                value = newValue
                newValue
            }
        }
    }
}
