package guru.zoroark.shedinja.test

import guru.zoroark.shedinja.dsl.put
import guru.zoroark.shedinja.dsl.shedinjaModule
import guru.zoroark.shedinja.test.check.ShedinjaCheckException
import guru.zoroark.shedinja.test.check.modules
import guru.zoroark.shedinja.test.check.shedinjaCheck
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

class ShedinjaCheckNoRules {
    @Test
    fun `Shedinja check without rules throws`() {
        val module = shedinjaModule {
            put(::ShedinjaCheckNoRules)
        }
        assertThrows<ShedinjaCheckException> {
            shedinjaCheck {
                modules(module)
            }
        }.assertMessage(
            """
            shedinjaCheck called without any rule, which checks nothing.
            --> Add rules using +ruleName (for example '+complete', do not forget the +)
            --> If you do not want to run any checks, remove the shedinjaCheck block entirely.
            For more information, visit https://shedinja.zoroark.guru/ShedinjaCheck
            """.trimIndent()
        )
    }
}

fun ShedinjaCheckException.assertMessage(expected: String) {
    assertEquals(expected, message)
}
