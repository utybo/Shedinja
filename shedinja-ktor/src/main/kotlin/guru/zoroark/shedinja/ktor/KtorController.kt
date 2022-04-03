package guru.zoroark.shedinja.ktor

import io.ktor.application.Application
import io.ktor.routing.Routing
import io.ktor.routing.routing

/**
 * Identical to [KtorModule], but allows you to directly add routes, as if calling `routing` in a [KtorModule].
 */
abstract class KtorController(priority: Int = DEFAULT_PRIORITY, restrictToAppName: String? = null) :
    KtorModule(priority, restrictToAppName) {
    /**
     * Install this controller's routes. You can call `route`, `get`, etc here.
     */
    abstract fun Routing.installController()

    override fun Application.installModule() {
        routing { installController() }
    }
}
