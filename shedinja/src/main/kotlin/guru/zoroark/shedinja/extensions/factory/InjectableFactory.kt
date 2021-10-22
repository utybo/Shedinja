package guru.zoroark.shedinja.extensions.factory

import guru.zoroark.shedinja.dsl.ContextBuilderDsl
import guru.zoroark.shedinja.dsl.ShedinjaDsl
import guru.zoroark.shedinja.dsl.put
import guru.zoroark.shedinja.environment.InjectionScope
import guru.zoroark.shedinja.environment.invoke
import guru.zoroark.shedinja.environment.wrapIn
import kotlin.properties.ReadOnlyProperty

/**
 * An injectable component whose job is to create components of the same type repeatably.
 */
fun interface InjectableFactory<T : Any> {
    /**
     * Create the component from the given requesting object (i.e. the object that asked for the injection).
     */
    fun make(requester: Any): T
}

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

// Injection DSL

/**
 * Initial DSL marker object for creating factory injection sites.
 *
 * @property ofObject The original caller for the factory creation. This is then passed in [InjectableFactory.make]'s
 * argument.
 */
class FactoryDsl(val ofObject: Any)

/**
 * Initiates the DSL for injecting factory-made objects. Usage is `factory from scope`.
 */
@ShedinjaDsl
val Any.factory
    get() = FactoryDsl(this)

/**
 * DSL for injecting factory-made objects. Usage is `factory from scope`.
 */
@ShedinjaDsl
inline infix fun <R, reified T : Any> FactoryDsl.from(scope: InjectionScope): ReadOnlyProperty<R, T> =
    scope<InjectableFactory<T>>(outputs(T::class)) wrapIn { it.make(ofObject) }
