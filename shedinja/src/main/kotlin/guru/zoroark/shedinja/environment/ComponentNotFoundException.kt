package guru.zoroark.shedinja.environment

import guru.zoroark.shedinja.ShedinjaException

/**
 * Exception thrown when a component is not found.
 */
class ComponentNotFoundException(message: String) : ShedinjaException(message)
