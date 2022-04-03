package guru.zoroark.shedinja.ktor

import io.ktor.application.Application

/**
 * A Ktor module that will be automatically installed to a corresponding [KtorApplication] with a matching
 * [app name][restrictToAppName].
 *
 * If you wish to add a controller (a module that only calls `routing` and adds routes), consider using [KtorController]
 * instead.
 */
abstract class KtorModule(
    /**
     * The installation priority of this module within the application. Modules are installed from largest to smallest
     * priority. The default value is available in the [DEFAULT_PRIORITY] constant.
     */
    val moduleInstallationPriority: Int = DEFAULT_PRIORITY,
    /**
     * The application name that this module should be installed in. This should correspond to the application name
     * specified in [KtorApplication.appName]
     */
    val restrictToAppName: String? = null
) {
    companion object {
        /**
         * The default priority for modules (see [KtorModule.moduleInstallationPriority]
         */
        const val DEFAULT_PRIORITY = 100
    }

    /**
     * Install this module on the given application.
     */
    abstract fun Application.installModule()
}
