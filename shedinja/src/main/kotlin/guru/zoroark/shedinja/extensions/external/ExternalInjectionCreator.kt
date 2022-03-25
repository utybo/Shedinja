package guru.zoroark.shedinja.extensions.external

import guru.zoroark.shedinja.dsl.ContextBuilderDsl
import guru.zoroark.shedinja.dsl.ShedinjaDsl
import guru.zoroark.shedinja.environment.EmptyQualifier
import guru.zoroark.shedinja.environment.Identifier
import guru.zoroark.shedinja.environment.InjectionScope
import guru.zoroark.shedinja.environment.Injector
import guru.zoroark.shedinja.environment.Qualifier
import guru.zoroark.shedinja.environment.plus
import guru.zoroark.shedinja.environment.wrapIn
import guru.zoroark.shedinja.extensions.DelegatedInjector
import guru.zoroark.shedinja.extensions.factory.outputs
import guru.zoroark.shedinja.extensions.injectors.AbstractQualifiableInjectionCreatorBuilder
import guru.zoroark.shedinja.extensions.injectors.InjectionCreator
import guru.zoroark.shedinja.extensions.injectors.InjectionCreatorKind
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

/**
 * A qualifiable injection creator that creates an external injector.
 *
 * This is the InjectionCreator equivalent of `external from scope`.
 */

class ExternalInjectionCreator<T : Any>(
    private val qualifier: Qualifier = EmptyQualifier,
    private val ofType: KClass<out T>? = null
) : InjectionCreator<T> {
    @Suppress("UNCHECKED_CAST")
    override fun createInjector(scope: InjectionScope, requestedClass: KClass<T>): Injector<T> {
        require(ofType == null || ofType.isSubclassOf(requestedClass)) {
            "Cannot create injector for $requestedClass because the specified type $ofType is not compatible"
        }

        return DelegatedInjector(
            scope.inject(
                Identifier(
                    ExternalComponentWrapper::class,
                    qualifier + outputs(requestedClass)
                ) as Identifier<ExternalComponentWrapper<T>>
            ) wrapIn { it.value }
        )
    }
}

class ExternalInjectionCreatorBuilder<T : Any> : AbstractQualifiableInjectionCreatorBuilder<T>() {
    override fun build(): InjectionCreator<T> = ExternalInjectionCreator(qualifier, lookedUpClassOrNull)
}

object ExternalInjectionCreatorKind : InjectionCreatorKind<ExternalInjectionCreatorBuilder<*>> {
    override fun <T : Any> createBuilder(): ExternalInjectionCreatorBuilder<T> = ExternalInjectionCreatorBuilder()
}

@ShedinjaDsl
val ContextBuilderDsl.external
    get() = ExternalInjectionCreatorKind
