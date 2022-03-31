package guru.zoroark.shedinja

import guru.zoroark.shedinja.environment.Identifier

/**
 * Type for exceptions directly emitted by Shedinja.
 *
 * @param message The message for this exception.
 */
@Suppress("UnnecessaryAbstractClass")
abstract class ShedinjaException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Exception thrown when an operation that requires an extensible injection environment was attempted on a
 * non-extensible environment.
 */
class NotExtensibleException(message: String) : ShedinjaException(message)

/**
 * Exception thrown when a component is not found.
 *
 * @property notFound The identifier of the component that was not found
 */
class ComponentNotFoundException(message: String, val notFound: Identifier<*>) : ShedinjaException(message) {
    constructor(notFound: Identifier<*>) : this("Component not found: $notFound", notFound)
}

/**
 * Exception thrown when a 'put' or another component declaration is invalid. A declaration can be invalid for any
 * number of reasons.
 */
class InvalidDeclarationException(message: String) : ShedinjaException(message)

/**
 * Exception thrown when something went wrong internally in Shedinja. Unless you are messing around with Shedinja's
 * internal, you should probably report occurrences of these exceptions (https://github.com/utybo/Shedinja/issues),
 * thanks!
 */
class InternalErrorException(message: String, throwable: Throwable? = null) : ShedinjaException(message, throwable)

/**
 * Thrown when an extension that needs to be installed was attempted to be used without being installed first.
 */
class ExtensionNotInstalledException(message: String) : ShedinjaException(message)
