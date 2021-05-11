package guru.zoroark.shedinja.environment

import kotlin.properties.ReadOnlyProperty

/**
 * Type for types that serve as injection components and injection sites.
 *
 * Subtypes should (but don't have to) implement this interface via delegation using a scope passed as a parameter, e.g.
 *
 * ```
 * class Service(scope: SComponent) : SComponent by scope {
 *     // ...
 * }
 * ```
 */
interface SComponent {
    /**
     * Create an injector for the given class, turned to an identifier. The behavior of the injection depends on the
     * [injection environment][InjectionEnvironment], but this is guaranteed to return an element of type [T].
     *
     * @param what The identifier to use for finding the relevant element.
     * @param T The type of the element to retrieve.
     * @return A read-only property which, on `get`, returns the relevant object.
     */
    fun <T : Any> inject(what: Identifier<T>): Injector<T>
}

/**
 * Create an injector for the given class, turned to an identifier. See [SComponent.inject] for more information. Use it
 * like so:
 *
 * ```
 * class Service(scope: SComponent) : SComponent by scope {
 *     val controller: Controller by inject()
 * }
 * ```
 */
inline fun <S : SComponent, reified T : Any> S.inject(): ReadOnlyProperty<S, T> {
    return inject(Identifier(T::class))
}
