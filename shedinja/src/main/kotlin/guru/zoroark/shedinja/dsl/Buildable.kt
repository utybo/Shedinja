package guru.zoroark.shedinja.dsl

/**
 * Represents a builder that can be turned into an object of type `T`.
 */
interface Buildable<T> {
    /**
     * Build the current object into a [BuildResult] object.
     */
    fun build(): BuildResult<T>
}

/**
 * Represents the result of a call to [Buildable.build]. This can either be:
 *
 * * a complete success ([Success])
 * * a success with some warnings ([SuccessWithWarnings])
 * * a failure with errors and optionally warnings ([Failure])
 *
 * Warnings and errors are represented by individual [BuildWarning] and [BuildError] objects
 */
sealed class BuildResult<T> {
    /**
     * Represents a successful [BuildResult] with a [result]
     *
     * @property result The result of the build
     */
    class Success<T>(val result: T) : BuildResult<T>()

    /**
     * Represents a successful [BuildResult] with a [result] and an additional, non-empty list of [warnings]
     *
     * @property result The result of the build
     * @property warnings Warnings that were emitted during the build process
     */
    class SuccessWithWarnings<T>(val result: T, val warnings: List<BuildWarning>) : BuildResult<T>()

    /**
     * Represents a failed [BuildResult] with non-empty [errors] and possibly-empty [warnings]
     *
     * @property errors The errors that made the build fail
     * @property warnings Warnings that were emitted during the build process
     */
    class Failure(val errors: List<BuildError>, val warnings: List<BuildWarning>) : BuildResult<Nothing>()
}

/**
 * A single warning that occurred during the build
 *
 * @property message The message for this warning
 */
data class BuildWarning(val message: String)

/**
 * A single error that occurred during the build
 *
 * @property message The message for this error
 */
data class BuildError(val message: String)
