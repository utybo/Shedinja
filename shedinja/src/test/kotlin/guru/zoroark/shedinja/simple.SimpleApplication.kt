package guru.zoroark.shedinja

import guru.zoroark.shedinja.dsl.put
import guru.zoroark.shedinja.dsl.shedinja
import guru.zoroark.shedinja.environment.SComponent
import guru.zoroark.shedinja.environment.get
import guru.zoroark.shedinja.environment.inject
import kotlin.test.Test
import kotlin.test.assertEquals

// This is an example of a simple Controller <--> Service <--> Repository setup

class Repository(scope: SComponent) : SComponent by scope {
    private var storage: String = "Unset"
    fun record(value: String) {
        storage = value
    }

    fun retrieve(): String {
        return storage
    }
}

class Service(scope: SComponent) : SComponent by scope {
    private val repo: Repository by inject()
    fun getElement(): String {
        return repo.retrieve()
    }

    fun setElement(str: String) {
        repo.record(str)
    }
}

class Controller(scope: SComponent) : SComponent by scope {
    private val service: Service by inject()

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
            put { Service(scope) }
            put { Repository(scope) }
        }
        val controller = env.get<Controller>()
        assertEquals("<p>Unset</p>", controller.makeElementHtml())
        controller.setElement("Hello!")
        assertEquals("<p>Hello!</p>", controller.makeElementHtml())
    }
}
