package guru.zoroark.shedinja.dsl

import guru.zoroark.shedinja.environment.InjectableModule
import guru.zoroark.shedinja.environment.InjectionEnvironment
import guru.zoroark.shedinja.environment.InjectionEnvironmentKind
import guru.zoroark.shedinja.environment.MixedImmutableEnvironment

/**
 * Entry point for the Shedinja DSL, used to build an injection environment.
 *
 * The lambda passed in this function receives an [EnvironmentContextBuilderDsl] object, which can be used to add
 * elements to the built environment.
 */
@ShedinjaDsl
fun shedinja(builder: ContextBuilderDsl.() -> Unit): MixedImmutableEnvironment =
    shedinja(MixedImmutableEnvironment, builder)

@ShedinjaDsl
fun <E : InjectionEnvironment> shedinja(
    environmentKind: InjectionEnvironmentKind<E>,
    builder: ContextBuilderDsl.() -> Unit
): E {
    val res = EnvironmentContextBuilderDsl().apply(builder).build()
    return environmentKind.build(res.getOrThrow())
}

@ShedinjaDsl
fun shedinjaModule(name: String = "<unnamed module>", builder: ContextBuilderDsl.() -> Unit): InjectableModule =
    ModuleBuilderDsl(name).apply(builder).build().getOrThrow()
