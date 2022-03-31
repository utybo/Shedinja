package guru.zoroark.shedinja.extensions

import guru.zoroark.shedinja.extensions.factory.outputs
import kotlin.test.Test
import kotlin.test.assertEquals

class TypeQualifierTest {
    class A
    @Test
    fun `Test qualifier toString`() {
        val a = outputs(A::class)
        assertEquals("Factory with output class guru.zoroark.shedinja.extensions.TypeQualifierTest\$A", a.toString())
        assertEquals(A::class, a.outputs)
    }
}
