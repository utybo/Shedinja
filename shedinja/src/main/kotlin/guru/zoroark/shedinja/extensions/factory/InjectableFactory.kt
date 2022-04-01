package guru.zoroark.shedinja.extensions.factory

import guru.zoroark.shedinja.dsl.ContextBuilderDsl
import guru.zoroark.shedinja.dsl.ShedinjaDsl
import guru.zoroark.shedinja.dsl.put
import guru.zoroark.shedinja.environment.InjectionScope
import guru.zoroark.shedinja.environment.Qualifier
import guru.zoroark.shedinja.environment.invoke
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * An injectable component whose job is to create components of the same type repeatably.
 */
fun interface InjectableFactory<T : Any> {
    /**
     * Create the component from the given requesting object (i.e. the object that asked for the injection).
     */
    fun make(requester: Any): T
}

// Environment management

/**
 * Qualifier specifically designed for [InjectableFactory]. Due to type erasure, all injectable factories are identified
 * as `InjectableFactory` by default, without any type parameter, leading to duplication errors since their identifiers
 * would all be the same. This qualifier lifts this ambiguity by providing the actual type of the output for
 * identification purposes.
 *
 * @property outputs The output type of this factory, i.e. the generic type of [InjectableFactory].
 */
data class InjectableFactoryOutputTypeQualifier(val outputs: KClass<*>) : Qualifier {
    override fun toString(): String = "Factory with output $outputs"
}

/**
 * Creates an [InjectableFactoryOutputTypeQualifier] with the given output as a parameter.
 */
@ShedinjaDsl
fun outputs(output: KClass<*>) = InjectableFactoryOutputTypeQualifier(output)

// Creation in module

/**
 * Allows to put a factory within the module or environment.
 *
 * A factory is the opposite of a singleton: instead of a single instance being created once and shared by all other
 * services (also known as a singleton), factories
 */
@ShedinjaDsl
inline fun <reified T : Any> ContextBuilderDsl.putFactory(crossinline block: (Any) -> T) {
    put(outputs(T::class)) { InjectableFactory { block(it) } }
}

/**
 * DSL for injecting factory-made objects. Usage is `factory from scope`.
 */
@ShedinjaDsl
inline fun <R : Any, reified T : Any> InjectionScope.factory(): ReadOnlyProperty<R, T> =
    this<InjectableFactory<T>>(outputs(T::class)) wrapInWithThisRef { thisRef, value -> value.make(thisRef as Any) }

// Utilities

/**
 * Utility function that wraps a given property using the given wrapper. This is useful when you want to transform the
 * output of a property in some way.
 *
 * This caches the result -- the `mapper` is executed lazily the first time the property is `get`ed.
 */
inline infix fun <T, V, R : Any> ReadOnlyProperty<T, V>.wrapIn(
    crossinline mapper: (V) -> R
): SynchronizedLazyPropertyWrapper<T, R> =
    SynchronizedLazyPropertyWrapper(WrappedReadOnlyProperty(this) { _, value -> mapper(value) })

/**
 * Identical to [wrapIn], but also gives you access to the object the property is owned by in the [mapper] lambda.
 */
inline infix fun <T, V, R : Any> ReadOnlyProperty<T, V>.wrapInWithThisRef(
    crossinline mapper: (T, V) -> R
): SynchronizedLazyPropertyWrapper<T, R> =
    SynchronizedLazyPropertyWrapper(WrappedReadOnlyProperty(this, mapper))

/**
 * Wraps a property and maps its result using the given mapper.
 */
@Suppress("FunctionName")
inline fun <T, V, R> WrappedReadOnlyProperty(
    original: ReadOnlyProperty<T, V>,
    crossinline mapper: (T, V) -> R
): ReadOnlyProperty<T, R> =
    ReadOnlyProperty { thisRef, property ->
        mapper(thisRef, original.getValue(thisRef, property))
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
