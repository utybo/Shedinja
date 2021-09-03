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

/**
 * Entry point for the Shedinja DSL, used to build an injection environment.
 *
 * The lambda passed in this function receives an [EnvironmentContextBuilderDsl] object, which can be used to add
 * elements to the built environment.
 *
 * This variation of the `shedinja` function allows you to specify a custom environment kind (see
 * [InjectionEnvironmentKind] for more information).
 *
 * @param environmentKind The environment builder that should be used.
 */
@ShedinjaDsl
fun <E : InjectionEnvironment> shedinja(
    environmentKind: InjectionEnvironmentKind<E>,
    builder: ContextBuilderDsl.() -> Unit
): E {
    val res = EnvironmentContextBuilderDsl().apply(builder).build()
    return environmentKind.build(res.getOrThrow())
}

/**
 * Creates a module using the Shedinja DSL with an optional name. You can then use the `put` function to add this module
 * to environment builders like the block in [shedinja].
 */
@ShedinjaDsl
fun shedinjaModule(name: String = "<unnamed module>", builder: ContextBuilderDsl.() -> Unit): InjectableModule =
    ModuleBuilderDsl(name).apply(builder).build().getOrThrow()
