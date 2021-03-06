package guru.zoroark.shedinja.test.check

import guru.zoroark.shedinja.environment.EnvironmentContext
import guru.zoroark.shedinja.environment.Identifier
import guru.zoroark.shedinja.environment.InjectionEnvironment
import guru.zoroark.shedinja.environment.InjectionEnvironmentKind
import guru.zoroark.shedinja.environment.InjectionScope
import guru.zoroark.shedinja.environment.Injector
import guru.zoroark.shedinja.environment.MetalessInjectionScope
import guru.zoroark.shedinja.environment.ScopedContext
import guru.zoroark.shedinja.test.NotAvailableInTestEnvironmentException
import kotlin.reflect.KProperty

private class FakeInjector<T : Any> : Injector<T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        throw NotAvailableInTestEnvironmentException("Not available")
    }
}

/**
 * Fake environment that tracks dependencies on the instantiation of components.
 *
 * Environment of this kind should rarely be created manually: they are used behind the scenes in rules that check for
 * the coherence of the contents of an environment (completeness check, cyclic dependency check...)
 */
class DependencyTrackingInjectionEnvironment(context: EnvironmentContext) : InjectionEnvironment {
    companion object : InjectionEnvironmentKind<DependencyTrackingInjectionEnvironment> {
        override fun build(context: EnvironmentContext): DependencyTrackingInjectionEnvironment =
            DependencyTrackingInjectionEnvironment(context)
    }

    override fun <T : Any> getOrNull(identifier: Identifier<T>): T? {
        throw NotAvailableInTestEnvironmentException("Not available on this kind of environment")
    }

    private val currentInjections = mutableListOf<Identifier<*>>()

    /**
     * The dependencies represented as a map from an identifier to the identifiers this identifier depends on.
     */
    val dependencies = context.declarations.mapValues { (_, v) ->
        currentInjections.clear()
        v.supplier(ScopedContext(EnvironmentBasedIgnoringMetaScope(this)))
        currentInjections.toList()
    }

    override fun <T : Any> createInjector(identifier: Identifier<T>, onInjection: (T) -> Unit): Injector<T> {
        currentInjections += identifier
        return FakeInjector()
    }
}

internal class EnvironmentBasedIgnoringMetaScope(
    private val environment: InjectionEnvironment
) : InjectionScope {
    override fun <T : Any> inject(what: Identifier<T>): Injector<T> {
        return environment.createInjector(what)
    }

    override val meta: MetalessInjectionScope
        get() = FakeMetalessInjectionScope()
}

private class FakeMetalessInjectionScope : MetalessInjectionScope {
    override fun <T : Any> inject(what: Identifier<T>): Injector<T> = FakeInjector()
}
