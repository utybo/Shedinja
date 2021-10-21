package guru.zoroark.shedinja.extensions

import guru.zoroark.shedinja.environment.Declaration
import guru.zoroark.shedinja.environment.Declarations
import guru.zoroark.shedinja.environment.EnvironmentBasedScope
import guru.zoroark.shedinja.environment.EnvironmentContext
import guru.zoroark.shedinja.environment.Identifier
import guru.zoroark.shedinja.environment.InjectionEnvironment
import guru.zoroark.shedinja.environment.InjectionEnvironmentKind
import guru.zoroark.shedinja.environment.Injector
import guru.zoroark.shedinja.environment.ScopedContext
import guru.zoroark.shedinja.environment.ensureInstance
import kotlin.reflect.KProperty

private data class EIEBeingBuiltInformation(
    val declarations: Declarations,
    val componentsBeingBuilt: MutableMap<Identifier<*>, Any>
)

private class StaticInjector<T : Any>(private val value: T) : Injector<T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value
    }
}

/**
 * An injection environment implementation with an eager evaluation strategy.
 *
 * Note that this environment is only intended for *meta-environments*. It is not intended for regular-purpose
 * (extensible) environments, which should instead use a
 * [MixedImmutableEnvironment][guru.zoroark.shedinja.environment.MixedImmutableEnvironment].
 *
 * ### Characteristics
 *
 * - **Eager object creation**. Objects are created upon construction of this environment.
 * - **Eager object injection**. Objects are injected upon calling the injection method.
 * - **Idempotent/Immutable**. Objects cannot be replaced, injection methods will always return the same thing.
 */
class EagerImmutableMetaEnvironment(context: EnvironmentContext) : InjectionEnvironment {
    companion object : InjectionEnvironmentKind<EagerImmutableMetaEnvironment> {
        override fun build(context: EnvironmentContext): EagerImmutableMetaEnvironment {
            return EagerImmutableMetaEnvironment(context)
        }
    }

    private val components = initializeComponents(context)
    private var buildingInformation: EIEBeingBuiltInformation? = null

    private fun initializeComponents(context: EnvironmentContext): Map<Identifier<*>, Any> {
        val componentsNow = mutableMapOf<Identifier<*>, Any>()
        buildingInformation = EIEBeingBuiltInformation(context.declarations, componentsNow)

        for ((_, declaration) in context.declarations) {
            initializeComponent(componentsNow, declaration)
        }
        buildingInformation = null
        return componentsNow.toMap()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getOrNull(identifier: Identifier<T>): T? {
        return components[identifier]?.also { ensureInstance(identifier.kclass, it) } as T?
    }

    override fun <T : Any> createInjector(identifier: Identifier<T>, onInjection: (T) -> Unit): Injector<T> {
        val info = buildingInformation
        if (info != null) {
            @Suppress("UNCHECKED_CAST")
            return StaticInjector(
                initializeComponent(
                    info.componentsBeingBuilt,
                    (info.declarations[identifier] ?: error("Component not found: $identifier")) as Declaration<T>
                ).also(onInjection)
            )
        } else {
            val value = getOrNull(identifier) ?: error("Component not found: $identifier")
            onInjection(value)
            return StaticInjector(value)
        }
    }
}

@Suppress("UNCHECKED_CAST")
private fun <T : Any> EagerImmutableMetaEnvironment.initializeComponent(
    components: MutableMap<Identifier<*>, Any>,
    declaration: Declaration<T>
): T {
    return components.getOrPut(declaration.identifier) {
        declaration.supplier(ScopedContext(EnvironmentBasedScope(this)))
    } as T
}
