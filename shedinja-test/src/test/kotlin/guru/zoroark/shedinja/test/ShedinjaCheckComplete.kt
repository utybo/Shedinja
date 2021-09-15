package guru.zoroark.shedinja.test

import guru.zoroark.shedinja.dsl.put
import guru.zoroark.shedinja.dsl.shedinjaModule
import guru.zoroark.shedinja.environment.InjectionScope
import guru.zoroark.shedinja.environment.invoke
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class ShedinjaCheckComplete {
    @Suppress("UnusedPrivateMember", "unused")
    class A(scope: InjectionScope) {
        private val b: B by scope()
    }

    class B

    @Suppress("UnusedPrivateMember", "unused")
    class C(scope: InjectionScope) {
        private val z: Z by scope()
    }

    @Suppress("UnusedPrivateMember", "unused")
    class E(scope: InjectionScope) {
        private val b: B by scope()
        private val c: C by scope()
        private val z: Z by scope()
    }

    @Suppress("UnusedPrivateMember", "unused")
    class F(scope: InjectionScope) {
        private val a: A by scope()
        private val z: Z by scope()
        private val y: Y by scope()
    }

    @Suppress("UnusedPrivateMember", "unused")
    class G(scope: InjectionScope) {
        private val y: Y by scope()
    }

    class Y
    class Z

    @Test
    fun `Test OK case`() {
        val module = shedinjaModule {
            put(::A)
            put(::B)
        }

        assertDoesNotThrow {
            shedinjaCheck {
                modules(module)

                +complete
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
                +complete
            }
        }.assertMessage(
            """
            'complete' check failed.
            Some dependencies were not found. Make sure they are present within your module definitions.
            --> guru.zoroark.shedinja.test.ShedinjaCheckComplete.Z (<no qualifier>) not found
                Requested by:
                --> guru.zoroark.shedinja.test.ShedinjaCheckComplete.C (<no qualifier>)
            """.trimIndent()
        )
    }

    @Test
    fun `Test many missing case`() {
        val module = shedinjaModule {
            put(::A)
            put(::B)
            put(::C)
            put(::E)
        }
        val module2 = shedinjaModule {
            put(::F)
            put(::G)
        }
        assertThrows<ShedinjaCheckException> {
            shedinjaCheck {
                modules(module, module2)
                +complete
            }
        }.assertMessage(
            """
            'complete' check failed.
            Some dependencies were not found. Make sure they are present within your module definitions.
            --> guru.zoroark.shedinja.test.ShedinjaCheckComplete.Z (<no qualifier>) not found
                Requested by:
                --> guru.zoroark.shedinja.test.ShedinjaCheckComplete.C (<no qualifier>)
                --> guru.zoroark.shedinja.test.ShedinjaCheckComplete.E (<no qualifier>)
                --> guru.zoroark.shedinja.test.ShedinjaCheckComplete.F (<no qualifier>)
            --> guru.zoroark.shedinja.test.ShedinjaCheckComplete.Y (<no qualifier>) not found
                Requested by:
                --> guru.zoroark.shedinja.test.ShedinjaCheckComplete.F (<no qualifier>)
                --> guru.zoroark.shedinja.test.ShedinjaCheckComplete.G (<no qualifier>)
            """.trimIndent()
        )
    }
}
