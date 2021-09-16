package guru.zoroark.shedinja.test

import guru.zoroark.shedinja.dsl.put
import guru.zoroark.shedinja.dsl.shedinjaModule
import guru.zoroark.shedinja.environment.EmptyQualifier
import guru.zoroark.shedinja.environment.InjectionScope
import guru.zoroark.shedinja.environment.Qualifier
import guru.zoroark.shedinja.environment.invoke
import guru.zoroark.shedinja.environment.named
import guru.zoroark.shedinja.test.check.NoUnusedCheckDsl
import guru.zoroark.shedinja.test.check.ShedinjaCheckException
import guru.zoroark.shedinja.test.check.exclude
import guru.zoroark.shedinja.test.check.modules
import guru.zoroark.shedinja.test.check.noUnused
import guru.zoroark.shedinja.test.check.shedinjaCheck
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import javax.swing.UIManager.put
import kotlin.test.Test

class ShedinjaCheckNoUnused {
    // Actually a cycle
    class A(scope: InjectionScope) {
        val b: B by scope()
    }

    class B(scope: InjectionScope) {
        val c: C by scope()
    }

    class C(scope: InjectionScope) {
        val a: A by scope()
    }

    class D(scope: InjectionScope) {
        val a: A by scope()
    }

    class E(scope: InjectionScope) {
        val a: A by scope()
    }

    @Test
    fun `Test regular situation`() {
        val module = shedinjaModule {
            put(::A)
            put(::B)
            put(::C)
        }
        assertDoesNotThrow {
            shedinjaCheck {
                modules(module)

                +noUnused
            }
        }
    }

    @Test
    fun `Test single unused situation`() {
        val module = shedinjaModule {
            put(::A)
            put(::B)
            put(::C)
            put(::D)
        }
        assertThrows<ShedinjaCheckException> {
            shedinjaCheck {
                modules(module)

                +noUnused
            }
        }.assertMessage(
            """
            'noUnused' check failed.
            The following component is not injected anywhere, making it unused.
            --> guru.zoroark.shedinja.test.ShedinjaCheckNoUnused.D (<no qualifier>)
            
            If some or all of the components mentioned above are still used outside of injections (e.g. via a 'get' ^
            call on the environment), you can exclude them from this rule by adding them after the 'noUnused':
            
                +noUnused {
                    exclude<ExcludeThis>()
                    exclude<ExcludeThat>(named("exclude.that"))
                    exclude(ExcludeIt::class)
                    exclude(ExcludeMe::class, named("excluded"))
                }
            """.trimIndent().replace("^\n", "")
        )
    }

    @Test
    fun `Test many unused situation`() {
        val module = shedinjaModule {
            put(::A)
            put(::B)
            put(::C)
            put(::D)
            put(::E)
        }
        assertThrows<ShedinjaCheckException> {
            shedinjaCheck {
                modules(module)

                +noUnused
            }
        }.assertMessage(
            """
            'noUnused' check failed.
            The following components are not injected anywhere, making them unused.
            --> guru.zoroark.shedinja.test.ShedinjaCheckNoUnused.D (<no qualifier>)
            --> guru.zoroark.shedinja.test.ShedinjaCheckNoUnused.E (<no qualifier>)
            
            If some or all of the components mentioned above are still used outside of injections (e.g. via a 'get' ^
            call on the environment), you can exclude them from this rule by adding them after the 'noUnused':
            
                +noUnused {
                    exclude<ExcludeThis>()
                    exclude<ExcludeThat>(named("exclude.that"))
                    exclude(ExcludeIt::class)
                    exclude(ExcludeMe::class, named("excluded"))
                }
            """.trimIndent().replace("^\n", "")
        )
    }

    private inline fun checkSuccessWithExclusion(
        dQualifier: Qualifier = EmptyQualifier,
        eQualifier: Qualifier = EmptyQualifier,
        crossinline dsl: NoUnusedCheckDsl.() -> Unit
    ) {
        val module = shedinjaModule {
            put(::A)
            put(::B)
            put(::C)
            put(dQualifier, ::D)
            put(eQualifier, ::E)
        }
        assertDoesNotThrow {
            shedinjaCheck {
                modules(module)

                +noUnused {
                    dsl()
                }
            }
        }
    }

    @Test
    fun `Ok with exclusion via reified, no qualifier`() {
        checkSuccessWithExclusion {
            exclude<D>()
            exclude<E>()
        }
    }

    @Test
    fun `Ok with exclusion via reified, with qualifier`() {
        checkSuccessWithExclusion(named("hello!"), named("bonjour !")) {
            exclude<D>(named("hello!"))
            exclude<E>(named("bonjour !"))
        }
    }

    @Test
    fun `Ok with exclusion via kclass, no qualifier`() {
        checkSuccessWithExclusion {
            exclude(D::class)
            exclude(E::class)
        }
    }

    @Test
    fun `Ok with exclusion via kclass, with qualifier`() {
        checkSuccessWithExclusion(named("goodbye!"), named("au revoir !")) {
            exclude(D::class, named("goodbye!"))
            exclude(E::class, named("au revoir !"))
        }
    }

    private inline fun checkFailureWithExclusion(
        dQualifier: Qualifier = EmptyQualifier,
        crossinline dsl: NoUnusedCheckDsl.() -> Unit
    ) {
        val module = shedinjaModule {
            put(::A)
            put(::B)
            put(::C)
            put(dQualifier, ::D)
            put(::E)
        }
        assertThrows<ShedinjaCheckException> {
            shedinjaCheck {
                modules(module)

                +noUnused {
                    dsl()
                }
            }
        }.assertMessage(
            """
            'noUnused' check failed.
            The following component is not injected anywhere, making it unused.
            --> guru.zoroark.shedinja.test.ShedinjaCheckNoUnused.E (<no qualifier>)
            
            If some or all of the components mentioned above are still used outside of injections (e.g. via a 'get' ^
            call on the environment), you can exclude them from this rule by adding them after the 'noUnused':
            
                +noUnused {
                    exclude<ExcludeThis>()
                    exclude<ExcludeThat>(named("exclude.that"))
                    exclude(ExcludeIt::class)
                    exclude(ExcludeMe::class, named("excluded"))
                }
            """.trimIndent().replace("^\n", "")
        )
    }

    @Test
    fun `Ok with exclusion via kclass, yet error somewhere else, reified, no qualifier`() {
        checkFailureWithExclusion {
            exclude<D>()
        }
    }

    @Test
    fun `Ok with exclusion via kclass, yet error somewhere else, kclass, no qualifier`() {
        checkFailureWithExclusion {
            exclude(D::class)
        }
    }

    @Test
    fun `Ok with exclusion via kclass, yet error somewhere else, reified, name qualifier`() {
        checkFailureWithExclusion(named("buongiorno!")) {
            exclude<D>(named("buongiorno!"))
        }
    }

    @Test
    fun `Ok with exclusion via kclass, yet error somewhere else, kclass, name qualifier`() {
        checkFailureWithExclusion(named("buongiorno!")) {
            exclude(D::class, named("buongiorno!"))
        }
    }
}
