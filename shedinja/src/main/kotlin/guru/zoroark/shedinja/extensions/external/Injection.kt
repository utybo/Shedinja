package guru.zoroark.shedinja.extensions.external

import guru.zoroark.shedinja.dsl.ShedinjaDsl
import guru.zoroark.shedinja.environment.EmptyQualifier
import guru.zoroark.shedinja.environment.InjectionEnvironment
import guru.zoroark.shedinja.environment.InjectionScope
import guru.zoroark.shedinja.environment.Qualifier
import guru.zoroark.shedinja.environment.get
import guru.zoroark.shedinja.environment.getOrNull
import guru.zoroark.shedinja.environment.invoke
import guru.zoroark.shedinja.environment.plus
import guru.zoroark.shedinja.environment.wrapIn
import guru.zoroark.shedinja.extensions.factory.outputs
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass

/**
 * Retrieves the external component with the given type and qualifier, or throws an exception if one could not be found.
 *
 * Uses a reified generic type to retrieve the component: you may use the other overloaded version if you want to use
 * a [KClass] instead.
 */
inline fun <reified T : Any> InjectionEnvironment.getExternal(qualifier: Qualifier = EmptyQualifier): T =
    get<ExternalComponentWrapper<T>>(outputs(T::class) + qualifier).value

/**
 * Retrieves the external component with the given type and qualifier, or throws an exception if one could not be found.
 *
 * Uses a [KClass] to retrieve the component: you may use the other overloaded version if you want to use a reified
 * generic type instead.
 */
fun <T : Any> InjectionEnvironment.getExternal(kclass: KClass<T>, qualifier: Qualifier = EmptyQualifier): T =
    get<ExternalComponentWrapper<T>>(outputs(kclass) + qualifier).value

/**
 * Retrieves the external component with the given type and qualifier, or returns null if one could not be found.
 *
 * Uses a reified generic type to retrieve the component: you may use the other overloaded version if you want to use
 * a [KClass] instead.
 */
inline fun <reified T : Any> InjectionEnvironment.getExternalOrNull(qualifier: Qualifier = EmptyQualifier): T? =
    getOrNull<ExternalComponentWrapper<T>>(outputs(T::class) + qualifier)?.value

/**
 * Retrieves the external component with the given type and qualifier, or returns null if one could not be found.
 *
 * Uses a [KClass] to retrieve the component: you may use the other overloaded version if you want to use a reified
 * generic type instead.
 */
fun <T : Any> InjectionEnvironment.getExternalOrNull(
    kclass: KClass<T>,
    qualifier: Qualifier = EmptyQualifier
): T? = getOrNull<ExternalComponentWrapper<T>>(outputs(kclass) + qualifier)?.value

/**
 * DSL object for the `external from scope` and `external(qualifier) from scope` syntax.
 */
class ExternalInjectionDsl(
    /**
     * The object that will be using the injection.
     */
    val ofObject: Any,
    /**
     * Additional qualifier for retrieving the external component.
     */
    val additionalQualifier: Qualifier = EmptyQualifier
)

/**
 * First part of the `external from scope` syntax. Use `external(qualifier)` instead if you want to have a qualifier.
 */
@ShedinjaDsl
val Any.external
    get() = ExternalInjectionDsl(this)

/**
 * First part of the `external(qualifier) from scope` syntax. Use `external` instead of you do not need qualifiers.
 */
@ShedinjaDsl
fun Any.external(additionalQualifier: Qualifier) = ExternalInjectionDsl(this, additionalQualifier)

/**
 * Creates an external component injector for the given type (and optional qualifier) within the given scope.
 */
@ShedinjaDsl
inline infix fun <R, reified T : Any> ExternalInjectionDsl.from(scope: InjectionScope): ReadOnlyProperty<R, T> =
    scope<ExternalComponentWrapper<T>>(outputs(T::class) + additionalQualifier) wrapIn { it.value }
