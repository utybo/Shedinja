package guru.zoroark.shedinja.extensions.factory

import guru.zoroark.shedinja.dsl.ShedinjaDsl
import guru.zoroark.shedinja.environment.Qualifier
import guru.zoroark.shedinja.environment.plus
import kotlin.reflect.KClass

/**
 * Qualifier specifically designed for extensions that wrap components.
 *
 * Due to type erasure, such components would be identified as their wrapping class by default, without any type
 * parameter, leading to duplication errors since their identifiers would all be the same. This qualifier lifts this
 * ambiguity by providing the actual class of the output for identification purposes.
 *
 * This should be a transparent process for end users: this qualifier should be applied transparently and, if necessary,
 * combined with any user-supplied qualifier using a [MultiQualifier][guru.zoroark.shedinja.environment.MultiQualifier]
 * (e.g. using [the `+` operator][Qualifier.plus] on the qualifiers)
 *
 * @property outputs The output type of this factory, i.e. the generic type of [InjectableFactory].
 */
data class OutputTypeQualifier(val outputs: KClass<*>) : Qualifier {
    override fun toString(): String = "outputs($outputs)"
}

/**
 * Creates an [OutputTypeQualifier] with the given output as a parameter.
 */
@ShedinjaDsl
fun outputs(output: KClass<*>) = OutputTypeQualifier(output)
