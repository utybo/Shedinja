package guru.zoroark.shedinja.environment

/**
 * An injection environment is, in a nutshell, a container for injectable components. These injectable components can be
 * retrieved in two ways:
 *
 * - Via the [get] function, which retrieves the component identified according to the given parameter
 * - Via the [createInjector] function, which returns an [Injector] that is able to retrieve injectable elements using a
 * Kotlin property delegator.
 *
 * ### Guarantees (or lack thereof)
 *
 * Internally, injection environments can use whatever mechanism they want. No guarantees are given on the mutability or
 * stability of the returned components -- it is up to the implementation of the injection environment to provide such
 * guarantees.
 *
 * All implementations should clearly state their characteristics in their documentation:
 *
 * - **Lazy** or **eager object creation**
 * - **Lazy**, **eager** or **active object injection**.
 * - **Idempotent/Immutable**, **NI/Immutable** or **NI/Mutable** (NI = non-idempotent).
 *
 * Here are the templates that should be used:
 *
 * - **Object creation**
 *      - **Eager object creation**. Objects are created upon construction of this environment.
 *      - **Lazy object creation**. Objects are created upon first use.
 * - **Object injection**
 *      - **Eager object injection**. Objects are injected upon calling the injection method.
 *      - **Lazy object injection**. Objects are injected upon first use, and are only computed once.
 *      - **Active object injection**. Objects are re-injected at every use.
 * - **Mutability**
 *      - **Idempotent/Immutable**. Objects cannot replaced, injection methods will always return the same thing.
 *      - **NI/Immutable**. Objects cannot be replaced, injection methods will not always return the same thing.
 *      - **NI/Mutable**. Objects can be replaced, injection methods will not always return the same thing.
 */
interface InjectionEnvironment {
    /**
     * Gets the component identified by the given identifier. No guarantees are given on this function - it may not be
     * idempotent, depending on the actual implementation.
     */
    fun <T : Any> get(identifier: Identifier<T>): T

    /**
     * Creates an [Injector] that can be used as a property delegator, bound against the given identifier.
     *
     * Using this function directly is not recommended. Use [inject] from within a [SComponent] instead
     *
     * @param T The injected component's type
     * @param identifier The identifier to create an injector for
     * @param onInjection Callback that must be called whenever the injection occurs. This is used for debugging and
     * testing purposes.
     */
    fun <T : Any> createInjector(identifier: Identifier<T>, onInjection: (T) -> Unit = {}): Injector<T>
}

/**
 * Gets the component identified by the given type turned into an [Identifier]. Refer to [InjectionEnvironment.get] for
 * more information.
 */
inline fun <reified T : Any> InjectionEnvironment.get(): T =
    get(Identifier(T::class))
