package guru.zoroark.shedinja.environment

import kotlin.properties.ReadOnlyProperty

/**
 * An injector is a read-only property delegator that has constraints on [S] and [T].
 *
 * Injectors are requested by component classes using any `inject` construct (e.g. [SComponent.inject] or [inject]). In
 * the environment, injectors are created using [InjectionEnvironment.createInjector].
 *
 * @param S The component to inject in (i.e. the injection site)
 * @param T The object type to inject
 */
interface Injector<in S : SComponent, out T : Any> : ReadOnlyProperty<S, T>
