package guru.zoroark.shedinja.extensions.injectors

import guru.zoroark.shedinja.dsl.ShedinjaDsl
import guru.zoroark.shedinja.environment.Qualifier
import kotlin.reflect.KClass

class ParameterQualifiableInjectionCreatorBuilder<T : Any>(
    val parameter: Int,
    private val actualBuilder: QualifiableInjectionCreatorBuilder<T>
) : QualifiableInjectionCreatorBuilder<T> {
    override fun build(): InjectionCreator<T> = actualBuilder.build()
    override fun addQualifier(qualifier: Qualifier) = actualBuilder.addQualifier(qualifier)
    override fun <V : T> setLookedUpClass(lookedUpClass: KClass<V>) = actualBuilder.setLookedUpClass(lookedUpClass)
}

@ShedinjaDsl
infix fun Int.inject(
    kind: InjectionCreatorKind<QualifiableInjectionCreatorBuilder<*>>
): ParameterQualifiableInjectionCreatorBuilder<*> =
    ParameterQualifiableInjectionCreatorBuilder(this, kind.createBuilder<Any>())

@ShedinjaDsl
val component = ComponentInjectionCreatorKind

@ShedinjaDsl
infix fun <B : QualifiableInjectionCreatorBuilder<out T>, T : Any> B.withQualifier(qualifier: Qualifier): B {
    addQualifier(qualifier)
    return this
}

inline operator fun <B : QualifiableInjectionCreatorBuilder<*>, T : Any> InjectionCreatorKind<B>.invoke(kclass: KClass<T>): InjectionCreatorKind<B> {
    @Suppress("UNCHECKED_CAST")
    return object : InjectionCreatorKind<B> {
        override fun <R : Any> createBuilder(): B = this@invoke.createBuilder<T>().apply {
            (this as QualifiableInjectionCreatorBuilder<T>).setLookedUpClass(kclass)
        }
    }
}
