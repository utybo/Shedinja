package guru.zoroark.shedinja.extensions.external

import guru.zoroark.shedinja.dsl.ContextBuilderDsl
import guru.zoroark.shedinja.dsl.ShedinjaDsl
import guru.zoroark.shedinja.dsl.put
import guru.zoroark.shedinja.environment.Declaration
import guru.zoroark.shedinja.environment.EmptyQualifier
import guru.zoroark.shedinja.environment.Qualifier
import guru.zoroark.shedinja.environment.plus
import guru.zoroark.shedinja.extensions.factory.outputs
import guru.zoroark.shedinja.extensions.injectors.InjectionCreator
import guru.zoroark.shedinja.extensions.injectors.ParameterQualifiableInjectionCreatorBuilder
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

/**
 * Add a definition for an external component of type `T` with the given constructor and parameter qualifiers.
 *
 * This overload uses reification to determine the type of `T`.
 *
 * See the documentation on the External Components Extension for more information.
 */
@ShedinjaDsl
inline fun <reified T : Any> ContextBuilderDsl.putExternal(
    constructor: KFunction<T>, vararg injectionCreators: ParameterQualifiableInjectionCreatorBuilder<*>
): Declaration<ExternalComponentWrapper<T>> =
    putExternal(T::class, EmptyQualifier, constructor, injectionCreators.build())

/**
 * Add a definition for an external component of type `T` with the given qualifier, constructor and parameter
 * qualifiers.
 *
 * This overload uses reification to determine the type of `T`.
 *
 * See the documentation on the External Components Extension for more information.
 */
@ShedinjaDsl
inline fun <reified T : Any> ContextBuilderDsl.putExternal(
    qualifier: Qualifier,
    constructor: KFunction<T>,
    vararg injectionCreators: ParameterQualifiableInjectionCreatorBuilder<*>
): Declaration<ExternalComponentWrapper<T>> =
    putExternal(T::class, qualifier, constructor, injectionCreators.build())

/**
 * Add a definition for an external component of type `T` with the given type, constructor and parameter qualifiers.
 *
 * This overload requires you to provide the class of T via the `kclass` parameter.
 *
 * See the documentation on the External Components Extension for more information.
 */
@ShedinjaDsl
fun <T : Any> ContextBuilderDsl.putExternal(
    kclass: KClass<T>,
    constructor: KFunction<T>,
    vararg injectionCreators: ParameterQualifiableInjectionCreatorBuilder<*>
): Declaration<ExternalComponentWrapper<T>> =
    putExternal(kclass, EmptyQualifier, constructor, injectionCreators.build())

/**
 * Add a definition for an external component of type `T` with the given type, constructor and parameter qualifiers.
 *
 * This overload requires you to provide the class of T via the `kclass` parameter.
 *
 * See the documentation on the External Components Extension for more information.
 */
@ShedinjaDsl
fun <T : Any> ContextBuilderDsl.putExternal(
    kclass: KClass<T>,
    qualifier: Qualifier,
    constructor: KFunction<T>,
    vararg injectionCreators: ParameterQualifiableInjectionCreatorBuilder<*>
): Declaration<ExternalComponentWrapper<T>> =
    putExternal(kclass, qualifier, constructor, injectionCreators.build())

/**
 * Add a definition for an external component of type `T` with the given type, constructor and parameter qualifiers,
 * passed as a map.
 *
 * This overload requires you to provide the class of T via the `kclass` parameter.
 *
 * See the documentation on the External Components Extension for more information.
 */
@ShedinjaDsl
fun <T : Any> ContextBuilderDsl.putExternal(
    kclass: KClass<T>, qualifier: Qualifier, constructor: KFunction<T>, injectionCreators: Map<Int, InjectionCreator<*>>
): Declaration<ExternalComponentWrapper<T>> = put(qualifier + outputs(kclass)) {
    ExternalComponentWrapper(scope, constructor, injectionCreators)
}

@PublishedApi
internal fun <T : ParameterQualifiableInjectionCreatorBuilder<*>> Array<T>.build() =
    associateBy({ it.parameter }) { it.build() }
