package guru.zoroark.shedinja.extensions.external

import guru.zoroark.shedinja.environment.EmptyQualifier
import guru.zoroark.shedinja.environment.Identifier
import guru.zoroark.shedinja.environment.InjectionScope
import guru.zoroark.shedinja.environment.Injector
import guru.zoroark.shedinja.environment.Qualifier
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty

/**
 * Wrapper class for using injection in external non-Shedinja components.
 *
 * You should not need to interact with this class directly. You should instead use the following functions:
 *
 * - For creating external components, use [putExternal] in your Shedinja environment.
 * - For getting external components from an environment, use [getExternal] or [getExternalOrNull] on your Shedinja
 *   environment.
 * - For injecting external components into regular Shedinja components, use the [external] [from] `scope` syntax.
 *
 * This class works by manually doing the process that regular Shedinja components do:
 *
 * - Create [injectors][Injector] on instantiation from the given scope.
 * - Actually retrieve stuff from injectors whenever necessary.
 */
class ExternalComponentWrapper<T : Any>(
    scope: InjectionScope,
    private val constructor: KFunction<T>,
    qualifiers: Map<Int, Qualifier>
) : ReadOnlyProperty<Any, T> {
    private val injectors: Map<KParameter, Injector<*>> = constructor.parameters.withIndex()
        .associateBy({ it.value }) { (index, it) ->
            scope.inject(it.identifyWith(qualifiers[index] ?: EmptyQualifier))
        }

    private val dummyProperty = Any()

    /**
     * The actual object wrapped by this class.
     */
    val value: T by lazy {
        val injectorValues =
            injectors.mapValues { it.value.getValue(this@ExternalComponentWrapper, this::dummyProperty) }
        constructor.callBy(injectorValues)
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): T = value
}

private fun KParameter.identifyWith(qualifier: Qualifier) =
    Identifier(this.type.classifier as KClass<*>, qualifier)
