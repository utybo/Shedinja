package guru.zoroark.shedinja.environment

import guru.zoroark.shedinja.dsl.ShedinjaDsl
import kotlin.reflect.KClass

/**
 * Identifies an injectable component via its type and optionally via other elements called [qualifiers][Qualifier].
 *
 * By default, identifiers use the [empty qualifier object][EmptyQualifier] as a way of saying "there is no qualifier
 * here". You generally do not need to use qualifiers if your environment only contains at most one object of a specific
 * type. If you do need multiple objects of the same type, qualifiers such as the [NameQualifier] should be used to
 * differentiate them.
 *
 * @property kclass The class this identifier wraps
 * @property qualifier The qualifier for this identifier.
 */
data class Identifier<T : Any>(val kclass: KClass<T>, val qualifier: Qualifier = EmptyQualifier) {
    override fun toString(): String {
        return (kclass.qualifiedName ?: "<anonymous>") + " ($qualifier)"
    }
}

/**
 * Qualifiers are simple objects that can be used within [Identifier] objects to provide additional differentiators
 * between two same-type objects or components.
 *
 * There are two built-in kinds of qualifiers:
 *
 * - The [EmptyQualifier], which is the type when no qualifier is used.
 * - The [NameQualifier], which is based on String objects.
 */
interface Qualifier {
    override fun toString(): String
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}

/**
 * The empty qualifier object must be used when there is no qualifier being used.
 *
 * This is the default qualifier object in all DSL functions if you do not specify any.
 */
object EmptyQualifier : Qualifier {
    override fun toString() =
        "<no qualifier>"

    override fun equals(other: Any?) =
        other === EmptyQualifier

    override fun hashCode(): Int = 1
}

/**
 * A qualifier that is based on a string. You can also use [named] to construct name qualifiers in a more DSL-ish
 * approach.
 *
 * @property name The name for this qualifier.
 */
data class NameQualifier(val name: String) : Qualifier

/**
 * Creates a [NameQualifier] with the given name.
 */
@ShedinjaDsl
fun named(name: String) = NameQualifier(name)
