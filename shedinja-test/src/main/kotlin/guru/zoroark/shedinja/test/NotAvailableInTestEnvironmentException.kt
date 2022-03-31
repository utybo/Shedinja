package guru.zoroark.shedinja.test

import guru.zoroark.shedinja.ShedinjaException

/**
 * Error thrown when a feature that does not exist in controlled test environments (e.g. environments internally used by
 * shedinjaCheck checks) is accessed. If you did not initiate the missing feature yourself:
 *
 * - Ensure that you are using only safe injections (see the `safeInjection` check)
 * - Otherwise, consider reporting it, as it may be a bug from Shedinja's checks.
 */
class NotAvailableInTestEnvironmentException(message: String) : ShedinjaException(message)
