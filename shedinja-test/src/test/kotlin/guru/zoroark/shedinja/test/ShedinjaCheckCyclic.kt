package guru.zoroark.shedinja.test

import guru.zoroark.shedinja.dsl.put
import guru.zoroark.shedinja.dsl.shedinjaModule
import guru.zoroark.shedinja.environment.InjectionScope
import guru.zoroark.shedinja.environment.invoke
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.fail
import kotlin.test.fail

class ShedinjaCheckCyclic {
    // Non-cyclic
    class Foo

    class Bar(scope: InjectionScope) {
        private val foo: Foo by scope()
    }

    // Cyclic A -> B -> A
    class A(scope: InjectionScope) {
        private val b: B by scope()
    }

    class B(scope: InjectionScope) {
        private val a: A by scope()
    }

    // Cyclic C -> D -> E -> F -> C
    //                    ------>
    class C(scope: InjectionScope) {
        private val d: D by scope()
    }

    class D(scope: InjectionScope) {
        private val e: E by scope()
    }

    class E(scope: InjectionScope) {
        private val f: F by scope()
        private val c: C by scope()
    }

    class F(scope: InjectionScope) {
        private val c: C by scope()
    }

    @Test
    fun `Test correct modules`() {
        val module = shedinjaModule {
            put(::Foo)
            put(::Bar)
        }
        assertDoesNotThrow {
            shedinjaCheck {
                modules(module)

                +noCycle
            }
        }

    }

    @Test
    fun `Test direct cycle`() {
        val module = shedinjaModule {
            put(::A)
            put(::B)
        }
        assertThrows<ShedinjaCheckException> {
            shedinjaCheck {
                modules(module)

                +noCycle
            }
        }
    }

    @Test
    fun `Test 4-way cycle`() {
        val module = shedinjaModule {
            put(::C)
            put(::D)
            put(::E)
            put(::F)
        }
        assertThrows<ShedinjaCheckException> {
            shedinjaCheck {
                modules(module)

                +noCycle
            }
        }
    }
}
