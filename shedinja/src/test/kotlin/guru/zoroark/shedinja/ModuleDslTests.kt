package guru.zoroark.shedinja

import guru.zoroark.shedinja.dsl.put
import guru.zoroark.shedinja.dsl.shedinjaModule
import guru.zoroark.shedinja.environment.Identifier
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ModuleDslTests {
    @Test
    fun `Test creating unnamed module`() {
        val module = shedinjaModule {
            put { ExampleClass(scope) }
            put { ExampleClass2(scope) }
        }
        assertEquals("<unnamed module>", module.name)
        assertEquals(2, module.declarations.size)
        assertEquals(Identifier(ExampleClass::class), module.declarations[0].identifier)
        assertEquals(Identifier(ExampleClass2::class), module.declarations[1].identifier)
    }

    @Test
    fun `Test creating named module`() {
        val module = shedinjaModule("Hello") {
            put { ExampleClass(scope) }
            put { ExampleClass2(scope) }
        }
        assertEquals("Hello", module.name)
        assertEquals(2, module.declarations.size)
        assertEquals(Identifier(ExampleClass::class), module.declarations[0].identifier)
        assertEquals(Identifier(ExampleClass2::class), module.declarations[1].identifier)
    }
}
