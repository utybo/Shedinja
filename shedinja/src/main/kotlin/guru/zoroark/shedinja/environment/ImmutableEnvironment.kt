package guru.zoroark.shedinja.environment

import kotlin.reflect.KClass
import kotlin.reflect.KProperty

private class EnvironmentBasedComponent(private val env: MixedImmutableEnvironment) : SComponent {
    override fun <T : Any> inject(what: Identifier<T>): Injector<T> {
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
 * - **Idempotent/Immutable**. Objects cannot be replaced, injection methods will always return the same thing.
 */
class MixedImmutableEnvironment(context: EnvironmentContext) : InjectionEnvironment {
    private val components = context.declarations.mapValues { (_, decl) ->
        decl.supplier(ScopedContext(EnvironmentBasedComponent(this)))
    }

    private inner class MIEInjector<T : Any>(
        private val identifier: Identifier<T>,
        private val onInjection: (T) -> Unit
    ) : Injector<T> {
        private val value by lazy {
            val result = components[identifier] ?: error("Component not found: ${identifier.kclass.qualifiedName}.")
            ensureInstance<T>(identifier.kclass, result).also { onInjection(it) }
        }

        override fun getValue(thisRef: SComponent, property: KProperty<*>): T = value
    }

    override fun <T : Any> get(identifier: Identifier<T>): T {
        val found = components[identifier] ?: error("No component found for ${identifier.kclass.qualifiedName}")
        return ensureInstance(identifier.kclass, found)
    }

    override fun <T : Any> createInjector(
        identifier: Identifier<T>,
        onInjection: (T) -> Unit
    ): Injector<T> =
        MIEInjector(identifier, onInjection)
}

private fun <T : Any> ensureInstance(kclass: KClass<*>, result: Any): T {
    if (!kclass.isInstance(result)) {
        error(
            "Internal error: injected component does not correspond to type expected by injector. " +
                    "Expected an injection of ${kclass.qualifiedName} but actually got ${result.javaClass.name}}"
        )
    }
    @Suppress("UNCHECKED_CAST") // The isInstance check is effectively the cast check
    return result as T
}
