package guru.zoroark.shedinja

import guru.zoroark.shedinja.environment.Declaration
import guru.zoroark.shedinja.environment.EnvironmentContext
import guru.zoroark.shedinja.environment.Identifier
import guru.zoroark.shedinja.environment.InjectionEnvironment
import guru.zoroark.shedinja.environment.InjectionScope
import guru.zoroark.shedinja.environment.ScopedSupplier
import guru.zoroark.shedinja.environment.get
import guru.zoroark.shedinja.environment.invoke
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

inline fun <reified T : Any> entryOf(noinline supplier: ScopedSupplier<T>) =
    Declaration(Identifier(T::class), supplier).let {
        it.identifier to it
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
            mapOf(entryOf {
                assertNull(element, "Called builder twice!")
                ElementClass().also { element = it }
            }, entryOf {
                assertNull(otherElement, "Called builder twice!")
                OtherElementClass().also { otherElement = it }
            }, entryOf {
                assertNull(anotherElement, "Called builder twice!")
                AnotherElementClass().also { anotherElement = it }
            })
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
            mapOf(entryOf {
                assertNull(a, "Called builder twice!")
                A(scope).also { a = it }
            }, entryOf {
                assertNull(b, "Called builder twice!")
                B(scope).also { b = it }
            }, entryOf {
                assertNull(c, "Called builder twice!")
                C().also { c = it }
            })
        )
        val env = provider(context)
        assertSame(a, env.get())
        assertSame(a?.b, env.get())
        assertSame(a?.b?.c, env.get())
        assertSame(b, env.get())
        assertSame(b?.c, env.get())
        assertSame(c, env.get())
    }

    fun eagerCreation(provider: (EnvironmentContext) -> InjectionEnvironment) {
        var wasFirstBuilt = false
        var wasSecondBuilt = false
        val context = EnvironmentContext(mapOf(
            entryOf { ElementClass().also { wasFirstBuilt = true } },
            entryOf { OtherElementClass().also { wasSecondBuilt = true } }
        ))
        provider(context)
        assertTrue(wasFirstBuilt)
        assertTrue(wasSecondBuilt)
    }
}
