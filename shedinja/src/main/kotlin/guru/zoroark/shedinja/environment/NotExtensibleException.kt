package guru.zoroark.shedinja.environment

import guru.zoroark.shedinja.ShedinjaException

/**
 * Exception thrown when an operation that requires an extensible injection environment was attempted on a
 * non-extensible environment.
 */
class NotExtensibleException(message: String) : ShedinjaException(message)
