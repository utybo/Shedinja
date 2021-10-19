package guru.zoroark.shedinja.extensions.services

/**
 * A service that will be started on request via the Shedinja [services extension][useServices].
 *
 * Note that, if implementors can use suspending functions, implementing [SuspendShedinjaService] should be preferred.
 */
interface ShedinjaService {
    /**
     * Start this Shedinja service.
     */
    fun start()

    /**
     * Stop this Shedinja service.
     */
    fun stop()
}

/**
 * A service that will be started on request via the Shedinja [services extension][useServices] and whose start and
 * stop functions use suspending functions.
 *
 * If implementors wish to use blocking mechanisms instead, [ShedinjaService] should be used instead.
 */
interface SuspendShedinjaService {
    /**
     * Start this Shedinja service.
     */
    suspend fun start()

    /**
     * Stop this Shedinja service.
     */
    suspend fun stop()
}
