package guru.zoroark.shedinja.environment

import kotlin.properties.ReadOnlyProperty

/**
 * An injection scope without meta-related operations. Because `InjectionScope.meta` itself returns (in theory) an
 * InjectionScope, people may do `scope.meta.meta.meta.meta...` to infinity and beyond, which does not make sense. In
 * order to prevent this, `InjectionScope.meta` instead returns a MetalessInjectionScope.
 */
interface MetalessInjectionScope {
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
 * An injection scope provides an entrypoint for components to retrieve the dependencies they need.
 *
 * This should be passed as a constructor parameter to components that require injection. Use the
 * [InjectionScope.invoke] operator to retrieve dependencies.
 */
interface InjectionScope : MetalessInjectionScope {
    /**
     * Returns another injection scope that provides access to the components in the meta-environment.
     *
     * For example:
     *
     * ```
     * class MyComponent(scope: InjectionScope) {
     *     val somethingFromMetaEnv: SomeExtensionComponent by scope.meta()
     * }
     * ```
     */
    val meta: MetalessInjectionScope
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
inline operator fun <reified T : Any> MetalessInjectionScope.invoke(
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
