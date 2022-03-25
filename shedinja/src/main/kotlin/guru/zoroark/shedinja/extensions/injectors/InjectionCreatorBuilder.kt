package guru.zoroark.shedinja.extensions.injectors

import guru.zoroark.shedinja.dsl.ShedinjaDsl
import guru.zoroark.shedinja.environment.EmptyQualifier
import guru.zoroark.shedinja.environment.Qualifier
import guru.zoroark.shedinja.environment.plus
import kotlin.reflect.KClass

interface InjectionCreatorBuilder<T : Any> {
    fun build(): InjectionCreator<T>
}


/**
 * An injection creator that can also receive additional qualifiers to further describe the wished injector.
 */
interface QualifiableInjectionCreatorBuilder<T : Any> : InjectionCreatorBuilder<T> {
    /**
     * Add a qualifier that will be used during the creation of the injector.
     */
    fun addQualifier(qualifier: Qualifier)

    /**
     * Sets the class that will be used to create the injector, similar to how one would specify a more specific typing
     * to a specific variable.
     *
     * Equivalent for the following pattern, where V is a subclass of T:
     *
     * ```
     * private val someVariable: T by scope<V>()
     * ```
     */
    fun <V : T> setLookedUpClass(lookedUpClass: KClass<V>)
}

abstract class AbstractQualifiableInjectionCreatorBuilder<T : Any> : QualifiableInjectionCreatorBuilder<T> {
    protected var lookedUpClassOrNull: KClass<out T>? = null
    protected var qualifier: Qualifier = EmptyQualifier

    override fun addQualifier(qualifier: Qualifier) {
        this.qualifier += qualifier
    }

    override fun <V : T> setLookedUpClass(lookedUpClass: KClass<V>) {
        lookedUpClassOrNull = lookedUpClass
    }
}

class ComponentInjectionCreatorBuilder<T : Any> : AbstractQualifiableInjectionCreatorBuilder<T>() {
    override fun build(): InjectionCreator<T> = ComponentInjectionCreator(qualifier, lookedUpClassOrNull)
}

interface InjectionCreatorKind<out B : QualifiableInjectionCreatorBuilder<*>> {
    fun <T : Any> createBuilder(): B
}

object ComponentInjectionCreatorKind : InjectionCreatorKind<ComponentInjectionCreatorBuilder<*>> {
    override fun <T : Any> createBuilder(): ComponentInjectionCreatorBuilder<T> =
        ComponentInjectionCreatorBuilder()
}
