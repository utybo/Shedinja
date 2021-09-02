package guru.zoroark.shedinja

import guru.zoroark.shedinja.environment.Identifier
import guru.zoroark.shedinja.environment.Injector
import guru.zoroark.shedinja.environment.SComponent

class ElementClass
class OtherElementClass
class AnotherElementClass

interface ExampleInterface
class ExampleClass(scope: SComponent) : ExampleInterface
class ExampleClass2(scope: SComponent)

object FakeComponent : SComponent {
    override fun <T : Any> inject(what: Identifier<T>): Injector<T> {
        error("Cannot inject on fake component")
    }

    val fakeProperty: Any? = null
}
