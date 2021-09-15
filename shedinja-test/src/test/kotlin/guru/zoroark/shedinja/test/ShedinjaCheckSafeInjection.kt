package guru.zoroark.shedinja.test

import guru.zoroark.shedinja.dsl.put
import guru.zoroark.shedinja.dsl.shedinjaModule
import guru.zoroark.shedinja.environment.InjectionScope
import guru.zoroark.shedinja.environment.invoke
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class ShedinjaCheckSafeInjection {
    @Suppress("UnusedPrivateMember", "unused")
    class A(scope: InjectionScope) {
        private val b: B by scope()
    }

    @Suppress("UnusedPrivateMember", "unused")
    class B(scope: InjectionScope) {
        private val a: A by scope()
    }

    @Suppress("UnusedPrivateMember", "unused")
    class C(scope: InjectionScope) {
        private val a: A by scope()
        private val b: B by scope()
    }

    @Suppress("UnusedPrivateMember", "unused")
    class D(scope: InjectionScope) {
        private val c: C by scope()
        private val cThing: C = c
    }

    @Test
    fun `Does not trigger on regular situation`() {
        val module = shedinjaModule {
            put(::A)
            put(::B)
            put(::C)
        }
        assertDoesNotThrow {
            shedinjaCheck {
                modules(module)

                +safeInjection
            }
        }
    }

    @Test
    @Suppress("MaxLineLength")
    fun `Triggers on dangerous injection`() {
        val module = shedinjaModule {
            put(::A)
            put(::B)
            put(::C)
            put(::D)
        }
        assertThrows<ShedinjaCheckException> {
            shedinjaCheck {
                modules(module)

                +safeInjection
            }
        }.assertMessage(
            """
            'safeInjection' check failed.
            The following injection is done during the instantiation of guru.zoroark.shedinja.test.ShedinjaCheckSafeInjection.D (<no qualifier>):
                guru.zoroark.shedinja.test.ShedinjaCheckSafeInjection.D (<no qualifier>)
            --> guru.zoroark.shedinja.test.ShedinjaCheckSafeInjection.C (<no qualifier>)
            You *must not* actually perform injections during the instantiation of objects.
            If you need to do something on an object provided by an environment before storing it as a property, use 'wrapInLazy' instead. See the documentation on the 'safeInjection' for more details.
            """.trimIndent()
        )
    }
}
