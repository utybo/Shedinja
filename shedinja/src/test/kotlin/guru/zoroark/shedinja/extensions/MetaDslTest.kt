package guru.zoroark.shedinja.extensions

import guru.zoroark.shedinja.dsl.put
import guru.zoroark.shedinja.dsl.shedinja
import guru.zoroark.shedinja.environment.get
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MetaDslTest {
    @Test
    fun `Test meta DSL`() {
        val env = shedinja {
            meta {
                put { "Hello" }
            }
        }
        assertEquals("Hello", env.metaEnvironment.get())
    }
}
