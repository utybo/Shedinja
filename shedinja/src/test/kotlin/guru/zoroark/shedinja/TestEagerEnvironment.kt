package guru.zoroark.shedinja

import guru.zoroark.shedinja.environment.EnvironmentContext
import guru.zoroark.shedinja.environment.Identifier
import guru.zoroark.shedinja.extensions.EagerImmutableMetaEnvironment
import kotlin.test.Test
import kotlin.test.assertTrue

class TestEagerEnvironment : EnvironmentBaseTest(::EagerImmutableMetaEnvironment) {
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
