package guru.zoroark.shedinja.environment

import guru.zoroark.shedinja.ElementClass
import guru.zoroark.shedinja.FakeComponent
import guru.zoroark.shedinja.OtherElementClass
import guru.zoroark.shedinja.entryOf
import guru.zoroark.shedinja.extensions.EagerImmutableMetaEnvironment
import kotlin.test.Test
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
}
