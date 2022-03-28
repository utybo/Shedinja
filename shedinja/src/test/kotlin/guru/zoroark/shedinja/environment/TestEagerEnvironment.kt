package guru.zoroark.shedinja.environment

import guru.zoroark.shedinja.ElementClass
import guru.zoroark.shedinja.FakeComponent
import guru.zoroark.shedinja.OtherElementClass
import guru.zoroark.shedinja.dsl.put
import guru.zoroark.shedinja.dsl.shedinja
import guru.zoroark.shedinja.entryOf
import guru.zoroark.shedinja.extensions.EagerImmutableMetaEnvironment
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestEagerEnvironment : NotExtensibleEnvironmentBaseTest(::EagerImmutableMetaEnvironment) {
    @Test
    fun `Test object injection is eager`() {
        val context = EnvironmentContext(
            mapOf(
                entryOf { ElementClass() },
                entryOf { OtherElementClass() }
            )
        )
        val env = EagerImmutableMetaEnvironment(context)
        var wasInjected = false
        val inj = env.createInjector(Identifier(ElementClass::class)) { wasInjected = true }
        assertTrue(wasInjected)
        inj.getValue(FakeComponent, FakeComponent::fakeProperty)
        assertTrue(wasInjected)
    }

    @Test
    fun `Test object injection after creation and does not exist throws`() {
        val context = EnvironmentContext(mapOf(entryOf { OtherElementClass() }))
        val env = EagerImmutableMetaEnvironment(context)
        val ex = assertThrows<ComponentNotFoundException> {
            env.createInjector(Identifier(ElementClass::class))
        }
        assertEquals(Identifier(ElementClass::class), ex.notFound)
    }

    @Test
    fun `Simple creation via DSL`() {
        val env = shedinja(EagerImmutableMetaEnvironment) {
            put { "Hello" }
        }
        assertEquals("Hello", env.get())
    }
}
