package guru.zoroark.shedinja.environment

import guru.zoroark.shedinja.dsl.ShedinjaDsl

/**
 * A qualifier that is based on a string. You can also use [named] to construct name qualifiers in a more DSL-ish
 * approach.
 *
 * @property name The name for this qualifier.
 */
data class NameQualifier(val name: String) : Qualifier {
    override fun toString(): String {
        return "named($name)"
    }
}

/**
 * Creates a [NameQualifier] with the given name.
 */
@ShedinjaDsl
fun named(name: String) = NameQualifier(name)
