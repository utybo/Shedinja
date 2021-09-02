package guru.zoroark.shedinja.full

import guru.zoroark.shedinja.dsl.put
import guru.zoroark.shedinja.dsl.shedinja
import guru.zoroark.shedinja.dsl.shedinjaModule
import guru.zoroark.shedinja.environment.SComponent
import guru.zoroark.shedinja.environment.get
import guru.zoroark.shedinja.environment.inject
import kotlin.test.Test
import kotlin.test.assertEquals

class ModuleBasedApplication {
    class HttpLibFacade {
        fun makeRequest(url: String): String =
            "Response to $url"
    }

    class HttpRepository(scope: SComponent) : SComponent by scope {
        private val facade: HttpLibFacade by inject()

        fun sendRequest(url: String): String =
            facade.makeRequest(url)

    }

    class HttpService(scope: SComponent) : SComponent by scope {
        private val repo: HttpRepository by inject()

        fun sendThatRequest(url: String) = repo.sendRequest(url)
    }

    class HttpController(scope: SComponent) : SComponent by scope {
        private val service: HttpService by inject()

        fun sendTheRequest(url: String) = service.sendThatRequest(url)
    }

    class SqlLibFacade {
        fun makeSqlRequest(url: String): String =
            "SQL Response to $url"
    }

    class SqlRepository(scope: SComponent) : SComponent by scope {
        private val facade: SqlLibFacade by inject()

        fun sendSqlRequest(url: String): String =
            facade.makeSqlRequest(url)
    }

    class SqlService(scope: SComponent) : SComponent by scope {
        private val repo: SqlRepository by inject()

        fun reallySendSqlRequest(url: String): String =
            repo.sendSqlRequest(url)
    }

    class SqlController(scope: SComponent) : SComponent by scope {
        private val service: SqlService by inject()

        fun reallyForRealSendSqlRequest(url: String): String =
            service.reallySendSqlRequest(url)
    }

    // ----------------------------
    // ---- Module definitions ----
    // ----------------------------

    private val httpModule = shedinjaModule("http") {
        put { HttpLibFacade() }
        put { HttpRepository(scope) }
        put { HttpService(scope) }
        put { HttpController(scope) }
    }

    private val sqlModule = shedinjaModule("sqlModule") {
        put(::SqlLibFacade)
        put(::SqlRepository)
        put(::SqlService)
        put(::SqlController)
    }

    @Test
    fun `Test simple module based application`() {
        val built = shedinja {
            put(httpModule)
            put(sqlModule)
        }
        val resSql = built.get<SqlController>().reallyForRealSendSqlRequest("coucou")
        assertEquals("SQL Response to coucou", resSql)

        val resHttp = built.get<HttpController>().sendTheRequest("hi")
        assertEquals("Response to hi", resHttp)
    }
}
