package guru.zoroark.shedinja.ktor.full

import guru.zoroark.shedinja.dsl.put
import guru.zoroark.shedinja.dsl.shedinja
import guru.zoroark.shedinja.environment.InjectionScope
import guru.zoroark.shedinja.extensions.services.services
import guru.zoroark.shedinja.extensions.services.useServices
import guru.zoroark.shedinja.ktor.KtorApplication
import guru.zoroark.shedinja.ktor.KtorApplicationSettings
import guru.zoroark.shedinja.ktor.KtorController
import guru.zoroark.shedinja.ktor.useKtor
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.client.features.ClientRequestException
import io.ktor.client.features.get
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.server.netty.Netty
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

private const val MULTI_APP_TEST_PORT_1 = 28821
private const val MULTI_APP_TEST_PORT_2 = 28822
private const val MULTI_APP_TEST_PORT_3 = 28823

//#region App 1

class ControllerOne : KtorController(restrictToAppName = "one") {
    override fun Routing.installController() {
        get("/one") {
            call.respondText("one")
        }
    }
}

class AppOne(scope: InjectionScope) : KtorApplication(scope, "one") {
    override val settings get() = KtorApplicationSettings(Netty, MULTI_APP_TEST_PORT_1)

    override fun Application.setup() {}
}

//#endregion

//#region App 2

class ControllerTwo : KtorController(restrictToAppName = "two") {
    override fun Routing.installController() {
        get("/two") {
            call.respondText("two")
        }
    }
}

class AppTwo(scope: InjectionScope) : KtorApplication(scope, "two") {
    override val settings get() = KtorApplicationSettings(Netty, MULTI_APP_TEST_PORT_2)

    override fun Application.setup() {}
}

//#endregion

//#region three

class ControllerThree : KtorController(restrictToAppName = "three") {
    override fun Routing.installController() {
        get("/three") {
            call.respondText("three")
        }
    }
}

class AppThree(scope: InjectionScope) : KtorApplication(scope, "three") {
    override val settings get() = KtorApplicationSettings(Netty, MULTI_APP_TEST_PORT_3)

    override fun Application.setup() {}
}

//#endregion

class MultiAppTest {
    @Test
    fun `Multi app test`() {
        val env = shedinja {
            useServices()
            useKtor()

            put(::AppOne)
            put(::AppTwo)
            put(::AppThree)

            put(::ControllerOne)
            put(::ControllerTwo)
            put(::ControllerThree)
        }
        runBlocking {
            env.services.startAll()

            val client = HttpClient(Java)

            assertEquals("one", client.get<String>("http://localhost:$MULTI_APP_TEST_PORT_1/one"))
            assertEquals("two", client.get<String>("http://localhost:$MULTI_APP_TEST_PORT_2/two"))
            assertEquals("three", client.get<String>("http://localhost:$MULTI_APP_TEST_PORT_3/three"))

            val endpointToPortsWhichShouldNotFound = mapOf(
                "one" to listOf(MULTI_APP_TEST_PORT_2, MULTI_APP_TEST_PORT_3),
                "two" to listOf(MULTI_APP_TEST_PORT_1, MULTI_APP_TEST_PORT_3),
                "three" to listOf(MULTI_APP_TEST_PORT_1, MULTI_APP_TEST_PORT_2)
            )
            for ((endpoint, ports) in endpointToPortsWhichShouldNotFound) {
                for (port in ports) {
                    assertNotFound { client.get<String>("http://localhost:$port/$endpoint") }
                }
            }

            env.services.stopAll()
        }
    }

    private inline fun assertNotFound(block: () -> Unit) {
        val ex = assertThrows<ClientRequestException> {
            block()
        }
        assertEquals(ex.response.status, HttpStatusCode.NotFound)
    }
}
