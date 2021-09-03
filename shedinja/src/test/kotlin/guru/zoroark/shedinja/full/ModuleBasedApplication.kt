package guru.zoroark.shedinja.full

import guru.zoroark.shedinja.dsl.put
import guru.zoroark.shedinja.dsl.shedinja
import guru.zoroark.shedinja.dsl.shedinjaModule
import guru.zoroark.shedinja.environment.InjectionScope
import guru.zoroark.shedinja.environment.get
import guru.zoroark.shedinja.environment.invoke
import kotlin.test.Test
import kotlin.test.assertEquals

class ModuleBasedApplication {
    class HttpLibFacade {
        fun makeRequest(url: String): String =
            "Response to $url"
    }

    class HttpRepository(scope: InjectionScope) {
        private val facade: HttpLibFacade by scope()

        fun sendRequest(url: String): String =
            facade.makeRequest(url)
    }

    class HttpService(scope: InjectionScope) {
        private val repo: HttpRepository by scope()

        fun sendThatRequest(url: String) = repo.sendRequest(url)
    }

    class HttpController(scope: InjectionScope) {
        private val service: HttpService by scope()

        fun sendTheRequest(url: String) = service.sendThatRequest(url)
    }

    class SqlLibFacade {
        fun makeSqlRequest(url: String): String =
            "SQL Response to $url"
    }

    class SqlRepository(scope: InjectionScope) {
        private val facade: SqlLibFacade by scope()

        fun sendSqlRequest(url: String): String =
            facade.makeSqlRequest(url)
    }

    class SqlService(scope: InjectionScope) {
        private val repo: SqlRepository by scope()

        fun reallySendSqlRequest(url: String): String =
            repo.sendSqlRequest(url)
    }

    class SqlController(scope: InjectionScope) {
        private val service: SqlService by scope()

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
