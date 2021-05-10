package guru.zoroark.shedinja.environment

import kotlin.reflect.KClass
import kotlin.reflect.KProperty

private class EnvironmentBasedComponent(private val env: MixedImmutableEnvironment) : SComponent {
    override fun <S : SComponent, T : Any> inject(what: Identifier<T>): Injector<S, T> {
        return env.createInjector(what)
    }
}

/**
 * An injection environment implementation with a *mixed evaluation strategy*.
 *
 * Mixed evaluation strategy here means that:
 * - Components are created eagerly, i.e. when this environment is created
 * - Injections are performed lazily within the same component, e.g. a component A that wants to have B injected will
 *   only actually get B when A performs a `get` on B.
 *
 * Mixed evaluation allows for lock-free thread safety for component creation and occasional locking thread safety on
 * injections.
 *
 * ### Characteristics
 *
 * - **Eager object creation**. Objects are created upon construction of this environment.
 * - **Lazy object injection**. Objects are injected upon first use, and are only computed once.
 * - **Idempotent/Immutable**. Objects cannot replaced, injection methods will always return the same thing.
 */
class MixedImmutableEnvironment(context: EnvironmentContext) : InjectionEnvironment {
    private val components = context.declarations.mapValues { (_, decl) ->
        decl.supplier(ScopedContext(EnvironmentBasedComponent(this)))
    }

    private inner class MIEInjector<S : SComponent, T : Any>(
        private val identifier: Identifier<T>
    ) : Injector<S, T> {
        private val value by lazy<T> {
            val result = components[identifier] ?: error("Component not found: ${identifier.kclass.qualifiedName}.")
            ensureInstance(identifier.kclass, result)
        }

        override fun getValue(thisRef: S, property: KProperty<*>): T = value
    }

    override fun <T : Any> get(identifier: Identifier<T>): T {
        val found = components[identifier] ?: error("No component found for ${identifier.kclass.qualifiedName}")
        return ensureInstance(identifier.kclass, found)
    }

    override fun <S : SComponent, T : Any> createInjector(identifier: Identifier<T>): Injector<S, T> =
        MIEInjector(identifier)
}

private fun <T : Any> ensureInstance(kclass: KClass<*>, result: Any): T {
    if (!kclass.isInstance(result))
        error("Internal error: injected component does not correspond to type expected by injector. " +
                "Expected an injection of ${kclass.qualifiedName} but actually got ${result.javaClass.name}}")
    @Suppress("UNCHECKED_CAST") // The isInstance check is effectively the cast check
    return result as T
}
