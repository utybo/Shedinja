package guru.zoroark.shedinja.extensions.services

import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("shedinja.services.shutdown")

/**
 * Adds a shutdown hook that will stop all the services when the JVM is shutting down.
 */
fun ServiceManager.installShutdownHook() {
    val hook = Thread {
        logger.warn("Received shutdown signal, stopping services...")
        runBlocking { stopAll(logger::info) }
        logger.info("done")
    }
    Runtime.getRuntime().addShutdownHook(hook)
    logger.debug("Shutdown hook installed")
}
