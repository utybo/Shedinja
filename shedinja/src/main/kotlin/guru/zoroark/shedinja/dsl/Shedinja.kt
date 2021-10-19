package guru.zoroark.shedinja.dsl

import guru.zoroark.shedinja.extensions.ExtensibleInjectionEnvironmentKind
import guru.zoroark.shedinja.environment.InjectableModule
import guru.zoroark.shedinja.environment.InjectionEnvironment
import guru.zoroark.shedinja.environment.InjectionEnvironmentKind
import guru.zoroark.shedinja.environment.MixedImmutableEnvironment
import guru.zoroark.shedinja.extensions.ExtensibleContextBuilderDsl
import guru.zoroark.shedinja.extensions.ExtensibleEnvironmentContextBuilderDsl
import guru.zoroark.shedinja.extensions.ExtensibleInjectionEnvironment

/**
 * Entry point for the Shedinja DSL, used to build an injection environment.
 *
 * The lambda passed in this function receives an [EnvironmentContextBuilderDsl] object, which can be used to add
 * elements to the built environment.
 *
 * This entry point is compatible with installable extensions.
 *
 * @returns A [MixedImmutableEnvironment] initialized using the given builder.
 */
@ShedinjaDsl
fun shedinja(builder: ExtensibleContextBuilderDsl.() -> Unit): MixedImmutableEnvironment =
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
 * This entry point is NOT compatible with installable extensions.
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
 * Entry point for the Shedinja DSL, used to build an injection environment.
 *
 * The lambda passed in this function receives an [EnvironmentContextBuilderDsl] object, which can be used to add
 * elements to the built environment.
 *
 * This variation of the `shedinja` function allows you to specify a custom environment kind (see
 * [InjectionEnvironmentKind] for more information).
 *
 * This entry point is compatible with installable extensions.
 *
 * @param environmentKind The environment builder that should be used.
 */
@ShedinjaDsl
fun <E : ExtensibleInjectionEnvironment> shedinja(
    environmentKind: ExtensibleInjectionEnvironmentKind<E>,
    builder: ExtensibleContextBuilderDsl.() -> Unit
): E {
    val res = ExtensibleEnvironmentContextBuilderDsl().apply(builder).build()
    return environmentKind.build(res.getOrThrow())
}

/**
 * Creates a module using the Shedinja DSL with an optional name. You can then use the `put` function to add this module
 * to environment builders like the block in [shedinja].
 */
@ShedinjaDsl
fun shedinjaModule(name: String = "<unnamed module>", builder: ContextBuilderDsl.() -> Unit): InjectableModule =
    ModuleBuilderDsl(name).apply(builder).build().getOrThrow()
