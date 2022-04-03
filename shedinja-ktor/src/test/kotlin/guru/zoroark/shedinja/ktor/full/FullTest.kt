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
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.server.netty.Netty
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

private const val TEST_PORT = 28899
private const val TEST_NAME = "MyApp"

data class ResponseData(val endpoint: String, val message: String)

class FirstController : KtorController(restrictToAppName = TEST_NAME) {
    override fun Routing.installController() {
        get("/test/one") {
            call.respond(ResponseData("one", "One!"))
        }
    }
}

class SecondController : KtorController(restrictToAppName = TEST_NAME) {
    override fun Routing.installController() {
        get("/test/two") {
            call.respond(ResponseData("two", "Two!!"))
        }
    }
}

class App(scope: InjectionScope) : KtorApplication(scope, TEST_NAME) {
    override val settings get() = KtorApplicationSettings(Netty, port = TEST_PORT)

    override fun Application.setup() {
        install(ContentNegotiation) { jackson() }
    }
}

class FullTest {
    @Test
    fun `Full application test`() {
        val env = shedinja {
            useServices()
            useKtor()

            put(::App)
            put(::FirstController)
            put(::SecondController)
        }
        runBlocking {
            env.services.startAll(::println)
            val client = HttpClient(Java) {
                install(JsonFeature) { serializer = JacksonSerializer() }
            }
            val result = client.get<ResponseData>("http://localhost:$TEST_PORT/test/one")
            assertEquals(ResponseData("one", "One!"), result)
            val result2 = client.get<ResponseData>("http://localhost:$TEST_PORT/test/two")
            assertEquals(ResponseData("two", "Two!!"), result2)
            env.services.stopAll(::println)
        }
    }
}
