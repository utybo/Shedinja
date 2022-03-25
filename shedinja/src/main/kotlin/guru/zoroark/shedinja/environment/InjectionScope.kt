package guru.zoroark.shedinja.environment

import kotlin.properties.ReadOnlyProperty

/**
 * An injection scope provides an entrypoint for components to retrieve the dependencies they need.
 *
 * This should be passed as a constructor parameter to components that require injection. Use the
 * [InjectionScope.invoke] operator to retrieve dependencies.
 */
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

    /**
     * Create an injector for the given identifier. Unlike `inject`, this function pulls the component from the *meta*
     * environment and not the current environment.
     *
     * Throws an exception if the current environment does not have a meta-environment.
     *
     * @param what The identifier to use for finding the relevant element.
     * @param T The type of the element to retrieve.
     * @return A read-only property which, on `get`, returns the relevant object.
     */
    fun <T : Any> meta(what: Identifier<T>): Injector<T>
}

/**
 * Create an injector for the given class, turned to an identifier, and an optional [qualifier][Qualifier].
 * See [InjectionScope.inject] for more information.
 *
 * ```
 * class Service(scope: InjectionScope) {
 *     val controller: Controller by scope()
 *     val repository: Repository by scope(named("my-repository"))
 * }
 * ```
 */
inline operator fun <reified T : Any> InjectionScope.invoke(
    qualifier: Qualifier = EmptyQualifier
): ReadOnlyProperty<Any?, T> =
    inject(Identifier(T::class, qualifier))

/**
 * Create an injector for the given [identifier][Identifier]. See [InjectionScope.inject] for more information.
 *
 * ```
 * class Service(scope: InjectionScope) {
 *     val controller by scope(Identifier(Controller::class))
 * }
 * ```
 */
operator fun <T : Any> InjectionScope.invoke(identifier: Identifier<T>): ReadOnlyProperty<Any?, T> =
    inject(identifier)

/**
 * Create an injector for the given class, turned to an identifier, and an optional [qualifier][Qualifier], which is
 * then matched against the *meta* environment. See [InjectionScope.meta] for more information.
 */
inline fun <reified T : Any> InjectionScope.meta(
    qualifier: Qualifier = EmptyQualifier
): ReadOnlyProperty<Any?, T> = meta(Identifier(T::class, qualifier))
