package guru.zoroark.shedinja

import guru.zoroark.shedinja.environment.ComponentNotFoundException
import guru.zoroark.shedinja.environment.Declaration
import guru.zoroark.shedinja.environment.EmptyQualifier
import guru.zoroark.shedinja.environment.EnvironmentContext
import guru.zoroark.shedinja.environment.Identifier
import guru.zoroark.shedinja.environment.InjectionEnvironment
import guru.zoroark.shedinja.environment.InjectionScope
import guru.zoroark.shedinja.environment.Qualifier
import guru.zoroark.shedinja.environment.ScopedSupplier
import guru.zoroark.shedinja.environment.get
import guru.zoroark.shedinja.environment.invoke
import guru.zoroark.shedinja.environment.named
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

inline fun <reified T : Any> entryOf(qualifier: Qualifier = EmptyQualifier, noinline supplier: ScopedSupplier<T>) =
    Declaration(Identifier(T::class, qualifier), supplier).let {
        it.identifier to it
    }

@Suppress("UnnecessaryAbstractClass")
abstract class EnvironmentBaseTest(private val provider: (EnvironmentContext) -> InjectionEnvironment) {
    @Test
    fun `(Basic) Put and get a single element`() {
        EnvironmentTests.singleElement(provider)
    }

    @Test
    fun `(Basic) Put and get multiple elements`() {
        EnvironmentTests.multiElements(provider)
    }

    @Test
    fun `(Basic) Put, get and inject multiple elements`() {
        EnvironmentTests.multiElementsWithInjections(provider)
    }

    @Test
    fun `(Basic) Put, get and inject multiple elements with qualifiers`() {
        EnvironmentTests.multiElementsWithInjectionsAndQualifiers(provider)
    }

    @Test
    fun `(Basic) Objects are created eagerly`() {
        EnvironmentTests.eagerCreation(provider)
    }

    @Test
    fun `(Basic) Getting unknown component should fail`() {
        val context = EnvironmentContext(
            mapOf(
                entryOf { ElementClass() }
            )
        )
        val env = provider(context)
        val ex = assertThrows<ComponentNotFoundException> {
            env.get<OtherElementClass>()
        }
        assertEquals(Identifier(OtherElementClass::class), ex.notFound)
    }

    @Test
    fun `(Basic) Injecting unknown component should fail`() {
        val context = EnvironmentContext(
            mapOf(
                entryOf { AtoB(scope) }
            )
        )
        val ex = assertThrows<ComponentNotFoundException> {
            val env = provider(context) // Eager envs will fail here
            val aToB = env.get<AtoB>()
            aToB.useB() // Lazy envs will fail here
        }
        assertEquals(Identifier(BtoA::class), ex.notFound)
    }
}

object EnvironmentTests {
    fun singleElement(provider: (EnvironmentContext) -> InjectionEnvironment) {
        var element: ElementClass? = null
        val context = EnvironmentContext(mapOf(entryOf { ElementClass().also { element = it } }))
        val env = provider(context)
        assertSame(element, env.get())
    }

    fun multiElements(provider: (EnvironmentContext) -> InjectionEnvironment) {
        var element: ElementClass? = null
        var otherElement: OtherElementClass? = null
        var anotherElement: AnotherElementClass? = null
        val context = EnvironmentContext(
            mapOf(
                entryOf {
                    assertNull(element, "Called builder twice!")
                    ElementClass().also { element = it }
                },
                entryOf {
                    assertNull(otherElement, "Called builder twice!")
                    OtherElementClass().also { otherElement = it }
                },
                entryOf {
                    assertNull(anotherElement, "Called builder twice!")
                    AnotherElementClass().also { anotherElement = it }
                }
            )
        )
        val env = provider(context)
        assertSame(element, env.get())
        assertSame(otherElement, env.get())
        assertSame(anotherElement, env.get())
    }

    class A(scope: InjectionScope) {
        val c: C by scope()
        val b: B by scope()
    }

    class B(scope: InjectionScope) {
        val c: C by scope()
    }

    class C

    fun multiElementsWithInjections(provider: (EnvironmentContext) -> InjectionEnvironment) {
        var a: A? = null
        var b: B? = null
        var c: C? = null
        val context = EnvironmentContext(
            mapOf(
                entryOf {
                    assertNull(a, "Called builder twice!")
                    A(scope).also { a = it }
                },
                entryOf {
                    assertNull(b, "Called builder twice!")
                    B(scope).also { b = it }
                },
                entryOf {
                    assertNull(c, "Called builder twice!")
                    C().also { c = it }
                }
            )
        )
        val env = provider(context)
        assertSame(a, env.get())
        assertSame(a?.b, env.get())
        assertSame(a?.b?.c, env.get())
        assertSame(b, env.get())
        assertSame(b?.c, env.get())
        assertSame(c, env.get())
    }

    class D(scope: InjectionScope) {
        val e: E by scope()
        val eBis: E by scope(named("eBis"))
    }

    class E(scope: InjectionScope) {
        val f1: F by scope(Identifier(F::class, named("f1")))
        val f2: F by scope(named("f2"))
    }

    class F

    fun multiElementsWithInjectionsAndQualifiers(provider: (EnvironmentContext) -> InjectionEnvironment) {
        var d: D? = null
        var e: E? = null
        var eBis: E? = null
        var f1: F? = null
        var f2: F? = null

        val context = EnvironmentContext(
            mapOf(
                entryOf {
                    assertNull(d, "Called builder twice!")
                    D(scope).also { d = it }
                },
                entryOf(named("f1")) {
                    assertNull(f1, "Called builder twice!")
                    F().also { f1 = it }
                },
                entryOf(named("f2")) {
                    assertNull(f2, "Called builder twice!")
                    F().also { f2 = it }
                },
                entryOf {
                    assertNull(e, "Called builder twice!")
                    E(scope).also { e = it }
                },
                entryOf(named("eBis")) {
                    assertNull(eBis, "Called builder twice!")
                    E(scope).also { eBis = it }
                }
            )
        )
        val env = provider(context)
        assertNotNull(f1)
        assertNotNull(f2)
        assertNotNull(e)
        assertNotNull(eBis)

        assertSame(d, env.get<D>())
        assertSame(f1, env.get<F>(named("f1")))
        assertSame(f2, env.get<F>(named("f2")))
        assertSame(e, env.get<E>())
        assertSame(eBis, env.get<E>(named("eBis")))

        assertSame(d?.e, e)
        assertSame(d?.eBis, eBis)

        assertSame(e?.f1, f1)
        assertSame(e?.f2, f2)

        assertSame(eBis?.f1, f1)
        assertSame(eBis?.f2, f2)
    }

    fun eagerCreation(provider: (EnvironmentContext) -> InjectionEnvironment) {
        var wasFirstBuilt = false
        var wasSecondBuilt = false
        val context = EnvironmentContext(
            mapOf(
                entryOf { ElementClass().also { wasFirstBuilt = true } },
                entryOf { OtherElementClass().also { wasSecondBuilt = true } }
            )
        )
        provider(context)
        assertTrue(wasFirstBuilt)
        assertTrue(wasSecondBuilt)
    }
}
