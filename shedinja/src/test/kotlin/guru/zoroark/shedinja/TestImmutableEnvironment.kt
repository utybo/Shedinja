package guru.zoroark.shedinja

import guru.zoroark.shedinja.environment.Declaration
import guru.zoroark.shedinja.environment.EnvironmentContext
import guru.zoroark.shedinja.environment.Identifier
import guru.zoroark.shedinja.environment.Injector
import guru.zoroark.shedinja.environment.MixedImmutableEnvironment
import guru.zoroark.shedinja.environment.SComponent
import guru.zoroark.shedinja.environment.ScopedSupplier
import guru.zoroark.shedinja.environment.get
import guru.zoroark.shedinja.environment.inject
import javax.swing.text.Element
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ElementClass
class OtherElementClass
class AnotherElementClass

object FakeComponent : SComponent {
    override fun <T : Any> inject(what: Identifier<T>): Injector<T> {
        error("Cannot inject on fake component")
    }

    val fakeProperty: Any? = null
}

class TestMixedEnvironment {

    private inline fun <reified T : Any> entryOf(noinline supplier: ScopedSupplier<T>) =
        Declaration(Identifier(T::class), supplier).let {
            it.identifier to it
        }


    @Test
    fun `Test with single element`() {
        var element: ElementClass? = null
        val context = EnvironmentContext(mapOf(entryOf { ElementClass().also { element = it } }))
        val env = MixedImmutableEnvironment(context)
        assertSame(element, env.get())
    }

    @Test
    fun `Test with many elements`() {
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
        val env = MixedImmutableEnvironment(context)
        assertSame(element, env.get())
        assertSame(otherElement, env.get())
        assertSame(anotherElement, env.get())
    }

    @Test
    fun `Test object creation is eager`() {
        var wasFirstBuilt = false
        var wasSecondBuilt = false
        val context = EnvironmentContext(mapOf(
            entryOf { ElementClass().also { wasFirstBuilt = true } },
            entryOf { OtherElementClass().also { wasSecondBuilt = true } }
        ))
        val env = MixedImmutableEnvironment(context)
        assertTrue(wasFirstBuilt)
        assertTrue(wasSecondBuilt)
    }

    @Test
    fun `Test object injection is lazy`() {
        // Laziness can only really be tested by using a cyclic dependency.
        val context = EnvironmentContext(mapOf(
            entryOf { ElementClass() },
            entryOf { OtherElementClass() }
        ))
        val env = MixedImmutableEnvironment(context)
        var wasInjected = false
        val inj = env.createInjector(Identifier(ElementClass::class)) { wasInjected = true }
        assertFalse(wasInjected)
        inj.getValue(FakeComponent, FakeComponent::fakeProperty)
        assertTrue(wasInjected)
    }
}
