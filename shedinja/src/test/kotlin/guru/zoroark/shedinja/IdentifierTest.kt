package guru.zoroark.shedinja

import guru.zoroark.shedinja.environment.Identifier
import kotlin.test.Test
import kotlin.test.assertEquals

class IdentifierTest {
    @Test
    fun `toString, non-anonymous object`() {
        val identifier = Identifier(IdentifierTest::class)
        assertEquals("guru.zoroark.shedinja.IdentifierTest (<no qualifier>)", identifier.toString())
    }

    @Test
    fun `toString, anonymous object`() {
        val anonymousObject = object {}
        val identifier = Identifier(anonymousObject::class)
        assertEquals("<anonymous> (<no qualifier>)", identifier.toString())
    }
}
