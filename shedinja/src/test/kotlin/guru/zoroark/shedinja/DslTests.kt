package guru.zoroark.shedinja

import com.sun.source.tree.Scope
import guru.zoroark.shedinja.dsl.BuildResult
import guru.zoroark.shedinja.dsl.EnvironmentBuilderDsl
import guru.zoroark.shedinja.dsl.put
import guru.zoroark.shedinja.environment.Identifier
import guru.zoroark.shedinja.environment.SComponent
import guru.zoroark.shedinja.environment.ScopedContext
import guru.zoroark.shedinja.environment.get
import kotlin.test.*

interface ExampleInterface
class ExampleClass(scope: SComponent) : ExampleInterface
class ExampleClass2(scope: SComponent)

class ShedinjaDslTests {

    @Test
    fun `Building from single-element builder works (without warnings)`() {
        val supplier: ScopedContext.() -> ExampleClass = { ExampleClass(scope) }
        val env = EnvironmentBuilderDsl().apply {
            put(supplier)
        }
        val built = env.build().assertSuccess()
        assertEquals(1, built.declarations.size)
        assertEquals(Identifier(ExampleClass::class), built.declarations.get<ExampleClass>().identifier)

    }

    @Test
    fun `Building from multi-elements builder works`() {
        val supplier: ScopedContext.() -> ExampleClass = { ExampleClass(scope) }
        val supplier2: ScopedContext.() -> ExampleClass2 = { ExampleClass2(scope) }
        val env = EnvironmentBuilderDsl().apply {
            put(supplier)
            put(supplier2)
        }
        val built = env.build().assertSuccess()
        assertEquals(2, built.declarations.size)
        assertEquals(Identifier(ExampleClass::class), built.declarations.get<ExampleClass>().identifier)
        assertEquals(Identifier(ExampleClass2::class), built.declarations.get<ExampleClass2>().identifier)
    }

    private fun <T> BuildResult<T>.assertSuccess(): T = when (this) {
        is BuildResult.Success -> this.result
        is BuildResult.SuccessWithWarnings -> fail("Expected a success, but got warnings:\n${warnings.joinToString("\n")}")
        is BuildResult.Failure -> fail("Expected a success, but failed with errors:\n${errors.joinToString("\n")}")
    }
}
