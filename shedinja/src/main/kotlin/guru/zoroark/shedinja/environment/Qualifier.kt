package guru.zoroark.shedinja.environment

/**
 * Qualifiers are simple objects that can be used within [Identifier] objects to provide additional differentiators
 * between two same-type objects or components.
 */
interface Qualifier {
    override fun toString(): String
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}
