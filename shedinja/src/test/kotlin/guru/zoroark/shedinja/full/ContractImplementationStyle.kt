package guru.zoroark.shedinja.full

import guru.zoroark.shedinja.dsl.put
import guru.zoroark.shedinja.dsl.shedinja
import guru.zoroark.shedinja.environment.InjectionScope
import guru.zoroark.shedinja.environment.get
import guru.zoroark.shedinja.environment.invoke
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ContractImplementationStyle {

    interface Repository {
        fun record(value: String)
        fun retrieve(): String
    }

    class RepositoryImpl : Repository {
        private var storage: String = "Unset"

        override fun record(value: String) {
            storage = value
        }

        override fun retrieve(): String {
            return storage
        }
    }

    interface Service {
        fun getElement(): String
        fun setElement(str: String)
    }

    class ServiceImpl(scope: InjectionScope) : Service {
        private val repo: Repository by scope()
        override fun getElement(): String {
            return repo.retrieve()
        }

        override fun setElement(str: String) {
            repo.record(str)
        }
    }

    interface Controller {
        fun makeElementHtml(): String
        fun setElement(newValue: String)
    }

    class ControllerImpl(scope: InjectionScope) : Controller {
        private val service: Service by scope()

        override fun makeElementHtml(): String {
            return "<p>${service.getElement()}</p>"
        }

        override fun setElement(newValue: String) {
            service.setElement(newValue)
        }
    }

    @Test
    fun `Test simple CSR model with contract style`() {
        val env = shedinja {
            put<Controller> { ControllerImpl(scope) }
            put<Service> { ServiceImpl(scope) }
            put<Repository> { RepositoryImpl() }
        }
        checkController(env.get())
        checkService(env.get())
        checkRepository(env.get())
    }

    private fun checkController(controller: Controller) {
        assertEquals("<p>Unset</p>", controller.makeElementHtml())
        controller.setElement("Hello!")
        assertEquals("<p>Hello!</p>", controller.makeElementHtml())
    }

    private fun checkService(service: Service) {
        assertEquals("Hello!", service.getElement())
        service.setElement("Goodbye!")
        assertEquals("Goodbye!", service.getElement())
    }

    private fun checkRepository(repository: Repository) {
        assertEquals("Goodbye!", repository.retrieve())
        repository.record("Goodbye for real!")
        assertEquals("Goodbye for real!", repository.retrieve())
    }
}
