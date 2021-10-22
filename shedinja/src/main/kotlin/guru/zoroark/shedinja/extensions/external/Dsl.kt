package guru.zoroark.shedinja.extensions.external

import guru.zoroark.shedinja.dsl.ContextBuilderDsl
import guru.zoroark.shedinja.dsl.ShedinjaDsl
import guru.zoroark.shedinja.dsl.put
import guru.zoroark.shedinja.environment.Declaration
import guru.zoroark.shedinja.environment.EmptyQualifier
import guru.zoroark.shedinja.environment.Qualifier
import guru.zoroark.shedinja.environment.plus
import guru.zoroark.shedinja.extensions.factory.outputs
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
    constructor: KFunction<T>,
    parameterQualifiers: Map<Int, Qualifier> = mapOf()
): Declaration<ExternalComponentWrapper<T>> =
    // EmptyQualifier because the subsequent putExternal call will add the outputs() for us
    putExternal(EmptyQualifier, constructor, parameterQualifiers)

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
    parameterQualifiers: Map<Int, Qualifier> = mapOf()
) = put(qualifier + outputs(T::class)) { ExternalComponentWrapper(scope, constructor, parameterQualifiers) }

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
    parameterQualifiers: Map<Int, Qualifier> = mapOf()
) = put(outputs(kclass)) { ExternalComponentWrapper(scope, constructor, parameterQualifiers) }

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
    parameterQualifiers: Map<Int, Qualifier> = mapOf()
) = put(qualifier + outputs(kclass)) { ExternalComponentWrapper(scope, constructor, parameterQualifiers) }
