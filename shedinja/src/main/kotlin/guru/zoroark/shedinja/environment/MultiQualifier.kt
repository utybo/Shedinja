package guru.zoroark.shedinja.environment

/**
 * A [qualifier][Qualifier] that represents a set of qualifiers. This can be used to have multiple qualifiers for a
 * single [identifier][Identifier].
 *
 * You can instantiate this class in two ways:
 *
 * - Manually creating a set of qualifiers, and using this class' constructor
 * - Using the [`+` operator][Qualifier.plus] on two (or more) qualifiers
 */
class MultiQualifier(
    /**
     * The qualifiers this object wraps.
     */
    val qualifiers: Set<Qualifier>
) : Qualifier {
    override fun toString(): String =
        qualifiers.joinToString(separator = " + ")

    override fun equals(other: Any?): Boolean =
        other is MultiQualifier && qualifiers == other.qualifiers

    override fun hashCode(): Int =
        qualifiers.hashCode()
}

/**
 * Combines the two qualifiers (hereafter q1 and q2) into another qualifier. The rules are as follows:
 *
 * - If q1 or q2 is an [EmptyQualifier], return the other one.
 * - If q1 or q2 (but not both) is a [MultiQualifier], return a [MultiQualifier] with the non-multi qualifier qualifier
 *   added.
 * - If q1 and q2  are both [MultiQualifiers][MultiQualifier], return a new multi-qualifier with the union of their
 *   sets.
 * - Otherwise, create a [MultiQualifier] with a set of two elements (q1 and q2).
 */
operator fun Qualifier.plus(other: Qualifier): Qualifier = when {
    this is EmptyQualifier ->
        other
    other is EmptyQualifier ->
        this
    this is MultiQualifier && other is MultiQualifier ->
        MultiQualifier(this.qualifiers union other.qualifiers)
    this is MultiQualifier ->
        MultiQualifier(this.qualifiers + other)
    other is MultiQualifier ->
        MultiQualifier(other.qualifiers + this)
    else ->
        MultiQualifier(setOf(this, other))
}
