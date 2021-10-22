package guru.zoroark.shedinja.environment

import guru.zoroark.shedinja.extensions.DefaultExtensibleInjectionEnvironment
import guru.zoroark.shedinja.extensions.EagerImmutableMetaEnvironment
import guru.zoroark.shedinja.extensions.ExtensibleEnvironmentContext
import guru.zoroark.shedinja.extensions.ExtensibleInjectionEnvironmentKind
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

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
 * Due to injections being made lazily, this environment supports cyclic dependencies (class A requires B, class B
 * requires A) and self-injections (class C requires itself).
 *
 * ### Characteristics
 *
 * - **Eager object creation**. Objects are created upon construction of this environment.
 * - **Lazy object injection**. Objects are injected upon first use, and are only computed once.
 * - **Idempotent/Immutable**. Objects cannot be replaced, injection methods will always return the same thing.
 *
 * Compatible with installable extensions.
 */
class MixedImmutableEnvironment(
    context: ExtensibleEnvironmentContext,
    metaEnvironmentKind: InjectionEnvironmentKind<*> = EagerImmutableMetaEnvironment
) : DefaultExtensibleInjectionEnvironment(context, metaEnvironmentKind) {

    companion object : ExtensibleInjectionEnvironmentKind<MixedImmutableEnvironment> {
        override fun build(context: ExtensibleEnvironmentContext, metaEnvironmentKind: InjectionEnvironmentKind<*>) =
            MixedImmutableEnvironment(context, metaEnvironmentKind)
    }

    private inner class MIEInjector<T : Any>(
        private val identifier: Identifier<T>,
        private val onInjection: (T) -> Unit
    ) : Injector<T> {
        private val value by lazy {
            val result = components[identifier] ?: error("Component not found: $identifier")
            ensureInstance(identifier.kclass, result).also(onInjection)
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): T = value
    }

    private val components = context.declarations.mapValues { (_, decl) ->
        decl.supplier(ScopedContext(EnvironmentBasedScope(this)))
    }

    override fun getAllIdentifiers(): Sequence<Identifier<*>> = components.keys.asSequence()

    override fun <T : Any> getOrNull(identifier: Identifier<T>): T? =
        components[identifier]?.let { ensureInstance(identifier.kclass, it) }

    override fun <T : Any> createInjector(
        identifier: Identifier<T>,
        onInjection: (T) -> Unit
    ): Injector<T> =
        MIEInjector(identifier, onInjection)
}

/**
 * Utility function - asserts that [obj] is an instance of [kclass]. Throws an exception if it is not or returns
 * [obj] cast to [T] on success.
 *
 * @param kclass The KClass of the expected type of [obj].
 * @param obj The object that should be tested.
 * @param T The expected type of [obj].
 */
fun <T : Any> ensureInstance(kclass: KClass<T>, obj: Any): T {
    require(kclass.isInstance(obj)) {
        "Object does not correspond to expected type. " +
                "Expected type ${kclass.qualifiedName} but got ${obj.javaClass.name}."
    }
    @Suppress("UNCHECKED_CAST") // The isInstance check is effectively the cast check
    return obj as T
}
