package guru.zoroark.shedinja.environment

import kotlin.properties.ReadOnlyProperty

interface InjectionScope {
    /**
     * Create an injector for the given identifier. The behavior of the injection depends on the
     * [injection environment][InjectionEnvironment], but this is guaranteed to return an element of type [T].
     *
     * @param what The identifier to use for finding the relevant element.
     * @param T The type of the element to retrieve.
     * @return A read-only property which, on `get`, returns the relevant object.
     */
    fun <T : Any> inject(what: Identifier<T>): Injector<T>
}

/**
 * Create an injector for the given class, turned to an identifier. See [InjectionScope.inject] for more information.
 *
 * ```
 * class Service(scope: InjectionScope) {
 *     val controller: Controller by scope()
 * }
 * ```
 */
inline operator fun <reified T : Any> InjectionScope.invoke(): ReadOnlyProperty<Any?, T> =
    inject(Identifier(T::class))

operator fun <T : Any> InjectionScope.invoke(identifier: Identifier<T>): ReadOnlyProperty<Any?, T> =
    inject(identifier)
