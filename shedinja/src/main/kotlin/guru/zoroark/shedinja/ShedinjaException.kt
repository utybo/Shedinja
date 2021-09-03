package guru.zoroark.shedinja

/**
 * Type for exceptions directly emitted by Shedinja.
 *
 * @param message The message for this exception.
 */
open class ShedinjaException(message: String) : Exception(message)
