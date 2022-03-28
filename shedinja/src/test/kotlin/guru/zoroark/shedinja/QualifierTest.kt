package guru.zoroark.shedinja

import guru.zoroark.shedinja.environment.named
import kotlin.test.Test
import kotlin.test.assertEquals

class QualifierTest {
    @Test
    fun `named DSL creates correct qualifier`() {
        val qualifier = named("one two three")
        assertEquals("one two three", qualifier.name)
    }
}
