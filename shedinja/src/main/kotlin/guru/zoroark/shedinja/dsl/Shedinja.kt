@file:Suppress("HttpUrlsUsage")

package guru.zoroark.shedinja.dsl

import guru.zoroark.shedinja.dsl.BuildResult
import guru.zoroark.shedinja.dsl.EnvironmentBuilderDsl
import guru.zoroark.shedinja.dsl.ShedinjaDsl
import guru.zoroark.shedinja.environment.InjectionEnvironment
import guru.zoroark.shedinja.environment.MixedImmutableEnvironment

/**
 * Entry point for the Shedinja DSL, used to build an injection environment.
 *
 * The lambda passed in this function receives an [EnvironmentBuilderDsl] object, which can be used to add elements to
 * the built environment.
 */
@ShedinjaDsl
fun shedinja(builder: EnvironmentBuilderDsl.() -> Unit): InjectionEnvironment {
    val res = EnvironmentBuilderDsl().apply(builder).build() as BuildResult.Success
    return MixedImmutableEnvironment(res.result)
}
