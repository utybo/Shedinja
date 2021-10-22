package guru.zoroark.shedinja.environment

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
