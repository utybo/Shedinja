package guru.zoroark.shedinja.environment

import guru.zoroark.shedinja.ShedinjaException

/**
 * Exception thrown when a component is not found.
 *
 * @property notFound The identifier of the component that was not found
 */
class ComponentNotFoundException(
    message: String,
    val notFound: Identifier<*>
) : ShedinjaException(message) {
    constructor(notFound: Identifier<*>) :
        this("Component not found: $notFound", notFound)
}
