package guru.zoroark.shedinja.test

import guru.zoroark.shedinja.dsl.put
import guru.zoroark.shedinja.dsl.shedinjaModule
import guru.zoroark.shedinja.environment.InjectionScope
import guru.zoroark.shedinja.environment.invoke
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.fail

class ShedinjaCheckComplete {
    class A(scope: InjectionScope) {
        private val b: B by scope()
    }

    class B

    class C(scope: InjectionScope) {
        private val d: D by scope()
    }

    class D

    @Test
    fun `Test OK case`() {
        val module = shedinjaModule {
            put(::A)
            put(::B)
        }

        assertDoesNotThrow {
            shedinjaCheck {
                modules(module)

                complete
            }
        }
    }

    @Test
    fun `Test single missing case`() {
        val module = shedinjaModule {
            put(::A)
            put(::B)
            put(::C)
        }
        assertThrows<ShedinjaCheckException> {
            shedinjaCheck {
                modules(module)

                complete
            }
        }
    }
}
