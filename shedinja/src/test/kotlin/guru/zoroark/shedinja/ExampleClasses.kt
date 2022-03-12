package guru.zoroark.shedinja

import guru.zoroark.shedinja.environment.Identifier
import guru.zoroark.shedinja.environment.InjectionScope
import guru.zoroark.shedinja.environment.Injector
import guru.zoroark.shedinja.environment.invoke

class ElementClass
class OtherElementClass
class AnotherElementClass

interface ExampleInterface
class ExampleClass : ExampleInterface
class ExampleClass2

object FakeComponent : InjectionScope {
    override fun <T : Any> inject(what: Identifier<T>): Injector<T> {
        error("Cannot inject on fake component")
    }

    val fakeProperty: Any? = null
}

class AtoB(scope: InjectionScope) {
    private val b: BtoA by scope()

    val className = "AtoB"

    fun useB() = b.className
}

class BtoA(scope: InjectionScope) {
    private val a: AtoB by scope()

    val className = "BtoA"

    fun useA() = a.className
}

class CtoC(scope: InjectionScope) {
    private val c: CtoC by scope()

    private val className = "CtoC"

    fun useC() = c.className
}
