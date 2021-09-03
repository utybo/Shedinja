package guru.zoroark.shedinja.full

import guru.zoroark.shedinja.dsl.put
import guru.zoroark.shedinja.dsl.shedinja
import guru.zoroark.shedinja.environment.InjectionScope
import guru.zoroark.shedinja.environment.get
import guru.zoroark.shedinja.environment.invoke
import kotlin.test.Test
import kotlin.test.assertEquals

// This is an example of a simple Controller <--> Service <--> Repository setup

class Repository {
    private var storage: String = "Unset"
    fun record(value: String) {
        storage = value
    }

    fun retrieve(): String {
        return storage
    }
}

class Service(scope: InjectionScope) {
    private val repo: Repository by scope()
    fun getElement(): String {
        return repo.retrieve()
    }

    fun setElement(str: String) {
        repo.record(str)
    }
}

class Controller(scope: InjectionScope) {
    private val service: Service by scope()

    fun makeElementHtml(): String {
        return "<p>${service.getElement()}</p>"
    }

    fun setElement(newValue: String) {
        service.setElement(newValue)
    }
}

class SimpleCSR {
    @Test
    fun `Test simple CSR model`() {
        val env = shedinja {
            put { Controller(scope) }
            put(::Service)
            put(::Repository)
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
