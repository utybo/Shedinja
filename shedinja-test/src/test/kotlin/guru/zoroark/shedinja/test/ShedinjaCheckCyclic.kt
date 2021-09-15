package guru.zoroark.shedinja.test

import guru.zoroark.shedinja.dsl.put
import guru.zoroark.shedinja.dsl.shedinjaModule
import guru.zoroark.shedinja.environment.InjectionScope
import guru.zoroark.shedinja.environment.invoke
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class ShedinjaCheckCyclic {
    // Non-cyclic
    class Foo

    @Suppress("UnusedPrivateMember", "unused")
    class Bar(scope: InjectionScope) {
        private val foo: Foo by scope()
    }

    // Cyclic A -> B -> A
    @Suppress("UnusedPrivateMember", "unused")
    class A(scope: InjectionScope) {
        private val b: B by scope()
    }

    @Suppress("UnusedPrivateMember", "unused")
    class B(scope: InjectionScope) {
        private val a: A by scope()
    }

    // Cyclic C -> D -> E -> F -> C
    //                    ------>
    @Suppress("UnusedPrivateMember", "unused")
    class C(scope: InjectionScope) {
        private val d: D by scope()
    }

    @Suppress("UnusedPrivateMember", "unused")
    class D(scope: InjectionScope) {
        private val e: E by scope()
    }

    @Suppress("UnusedPrivateMember", "unused")
    class E(scope: InjectionScope) {
        private val f: F by scope()
        private val c: C by scope()
    }

    @Suppress("UnusedPrivateMember", "unused")
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
        }.assertMessage(
            """
            'noCycle' check failed.
            Cyclic dependency found:
                guru.zoroark.shedinja.test.ShedinjaCheckCyclic.A (<no qualifier>)
            --> guru.zoroark.shedinja.test.ShedinjaCheckCyclic.B (<no qualifier>)
            --> guru.zoroark.shedinja.test.ShedinjaCheckCyclic.A (<no qualifier>)
            Note: --> represents an injection (i.e. A --> B means 'A depends on B').
            """.trimIndent()
        )
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
        }.assertMessage(
            """
            'noCycle' check failed.
            Cyclic dependency found:
                guru.zoroark.shedinja.test.ShedinjaCheckCyclic.C (<no qualifier>)
            --> guru.zoroark.shedinja.test.ShedinjaCheckCyclic.D (<no qualifier>)
            --> guru.zoroark.shedinja.test.ShedinjaCheckCyclic.E (<no qualifier>)
            --> guru.zoroark.shedinja.test.ShedinjaCheckCyclic.F (<no qualifier>)
            --> guru.zoroark.shedinja.test.ShedinjaCheckCyclic.C (<no qualifier>)
            Note: --> represents an injection (i.e. A --> B means 'A depends on B').
            """.trimIndent()
        )
    }
}
