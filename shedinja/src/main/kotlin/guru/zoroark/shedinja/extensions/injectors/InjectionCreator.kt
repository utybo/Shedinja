package guru.zoroark.shedinja.extensions.injectors

import guru.zoroark.shedinja.environment.EmptyQualifier
import guru.zoroark.shedinja.environment.Identifier
import guru.zoroark.shedinja.environment.InjectionScope
import guru.zoroark.shedinja.environment.Injector
import guru.zoroark.shedinja.environment.Qualifier
import guru.zoroark.shedinja.extensions.external.ExternalComponentWrapper
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

/**
 * Injection creators are advanced extension components that allow for the description and creation of special
 * injectors. This is the preferred way of generically passing "injection descriptions" around.
 *
 * A typical use case for these can be found in the external extension, which dynamically creates injectors based on
 * what non-Shedinja components need in their constructor. While creating regular injectors solely based on a `KClass`
 * and a qualifier is easy, similar to a `by scope()` call:
 *
 * - How can external components understand how to fulfill special injection requirements, such as injecting something
 *   actually provided by a factory or another external components?
 * - How can this "injection contract" be passed generically? For example, extensions already use `xxx from scope` as a
 *   "standard" for injecting special components, but how can this be easily described in a flexible and extensible way?
 *
 * Injection creators aim to solve part of this problem by providing interfaces, classes and a DSL so that users can
 * easily specify injectors. For example, "I want this parameter of the external constructor to be fulfilled by this
 * other external thing."
 *
 * At a high level, Injection creator are the link between the injectors of high-level type wrappers (e.g. "an external
 * Database component") and the actual class that wraps it (e.g. [ExternalComponentWrapper])
 *
 * All pure extensions that "wrap" object instances around another class (such as the factory extension or the external
 * extension) should implement a [QualifiableInjectionCreator] and the appropriate DSL function.
 */
interface InjectionCreator<T : Any> {
    /**
     * Creates an injector using the given scope for the given `KClass`.
     */
    fun createInjector(scope: InjectionScope, requestedClass: KClass<T>): Injector<T>
}

/**
 * The most basic kind of injection creator. This kind of injection creator should be the default behavior for any
 * "put-your-injector-here" scenario where only the `KClass` is known. This creates an injector based on a simple
 * regular component.
 */
class ComponentInjectionCreator<T : Any>(
    private val qualifier: Qualifier = EmptyQualifier,
    private val ofType: KClass<out T>? = null
): InjectionCreator<T> {
    override fun createInjector(scope: InjectionScope, requestedClass: KClass<T>): Injector<T> {
        require(ofType == null || ofType.isSubclassOf(requestedClass)) {
            "Cannot create injector for $requestedClass because the specified type $ofType is not compatible"
        }

        return scope.inject(Identifier(ofType ?: requestedClass, qualifier))
    }
}

