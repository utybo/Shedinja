package guru.zoroark.shedinja

import guru.zoroark.shedinja.environment.EnvironmentContext
import guru.zoroark.shedinja.environment.Identifier
import guru.zoroark.shedinja.environment.InjectionScope
import guru.zoroark.shedinja.environment.MixedImmutableEnvironment
import guru.zoroark.shedinja.environment.get
import guru.zoroark.shedinja.environment.invoke
import guru.zoroark.shedinja.extensions.ExtensibleEnvironmentContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestMixedEnvironment : ExtensibleEnvironmentBaseTest({
    MixedImmutableEnvironment.build(it)
}) {
    @Test
    fun `Test object injection is lazy`() {
        val context = ExtensibleEnvironmentContext(
            mapOf(
                entryOf { ElementClass() },
                entryOf { OtherElementClass() }
            ),
            EnvironmentContext(mapOf())
        )
        val env = MixedImmutableEnvironment(context)
        var wasInjected = false
        val inj = env.createInjector(Identifier(ElementClass::class)) { wasInjected = true }
        assertFalse(wasInjected)
        inj.getValue(FakeComponent, FakeComponent::fakeProperty)
        assertTrue(wasInjected)
    }

    class AtoB(scope: InjectionScope) {
        private val b: BtoA by scope()

        val className = "AtoB"

        fun useB() = b.className
    }

    class BtoA(scope: InjectionScope) {
        private val a: AtoB by scope()

        val className = "BtoA"

        fun useA() = a.className
    }

    class CtoC(scope: InjectionScope) {
        private val c: CtoC by scope()

        private val className = "CtoC"

        fun useC() = c.className
    }

    @Test
    fun `Test supports cyclic dependencies`() {
        val context = ExtensibleEnvironmentContext(
            mapOf(
                entryOf { AtoB(scope) },
                entryOf { BtoA(scope) }
            ),
            EnvironmentContext(mapOf())
        )
        val env = MixedImmutableEnvironment(context)
        val a = env.get<AtoB>()
        val b = env.get<BtoA>()
        assertEquals("BtoA", a.useB())
        assertEquals("AtoB", b.useA())
    }

    @Test
    fun `Test supports self injection`() {
        val context = ExtensibleEnvironmentContext(
            mapOf(
                entryOf { CtoC(scope) }
            ),
            EnvironmentContext(mapOf())
        )
        val env = MixedImmutableEnvironment(context)
        val c = env.get<CtoC>()
        assertEquals("CtoC", c.useC())
    }
}
