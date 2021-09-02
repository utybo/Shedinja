package guru.zoroark.shedinja

import guru.zoroark.shedinja.dsl.BuildResult
import guru.zoroark.shedinja.dsl.EnvironmentContextBuilderDsl
import guru.zoroark.shedinja.dsl.put
import guru.zoroark.shedinja.environment.Identifier
import guru.zoroark.shedinja.environment.SComponent
import guru.zoroark.shedinja.environment.ScopedContext
import guru.zoroark.shedinja.environment.get
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ShedinjaDslTests {
    @Test
    fun `Building from single-element builder works (without warnings)`() {
        val supplier: ScopedContext.() -> ExampleClass = { ExampleClass(scope) }
        val env = EnvironmentContextBuilderDsl().apply {
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
        val env = EnvironmentContextBuilderDsl().apply {
            put(supplier)
            put(supplier2)
        }
        val built = env.build().assertSuccess()
        assertEquals(2, built.declarations.size)
        assertEquals(Identifier(ExampleClass::class), built.declarations.get<ExampleClass>().identifier)
        assertEquals(Identifier(ExampleClass2::class), built.declarations.get<ExampleClass2>().identifier)
    }

    @Test
    fun `Building from constructor references works`() {
        class NoConstructor
        class GoodConstructor(val scope: SComponent)
        val built = EnvironmentContextBuilderDsl().apply {
            put(::NoConstructor)
            put(::GoodConstructor)
        }.build().assertSuccess()

    }

    @Test
    fun `Duplicate via inferred type put should throw error`() {
        val ex = assertThrows<ShedinjaException> {
            EnvironmentContextBuilderDsl().apply {
                put { ExampleClass(scope) }
                put { ExampleClass(scope) }
            }
        }
        assertEquals(
            "Duplicate identifier: Tried to put 'guru.zoroark.shedinja.ExampleClass', but one was already present",
            ex.message
        )
    }

    @Test
    fun `Duplicate via class put should throw error`() {
        val ex = assertThrows<ShedinjaException> {
            EnvironmentContextBuilderDsl().apply {
                put(ExampleClass::class) { ExampleClass(scope) }
                put(ExampleClass::class) { ExampleClass(scope) }
            }
        }
        assertEquals(
            "Duplicate identifier: Tried to put 'guru.zoroark.shedinja.ExampleClass', but one was already present",
            ex.message
        )
    }

    @Test
    fun `Duplicate via class and inferred type put should throw error`() {
        val ex = assertThrows<ShedinjaException> {
            EnvironmentContextBuilderDsl().apply {
                put(ExampleClass::class) { ExampleClass(scope) }
                put { ExampleClass(scope) }
            }
        }
        assertEquals(
            "Duplicate identifier: Tried to put 'guru.zoroark.shedinja.ExampleClass', but one was already present",
            ex.message
        )
    }
}

private fun <T> BuildResult<T>.assertSuccess(): T = when (this) {
    is BuildResult.Success -> this.result
    is BuildResult.SuccessWithWarnings -> fail("Expected a success, but got warnings:\n${warnings.joinToString("\n")}")
    is BuildResult.Failure -> fail("Expected a success, but failed with errors:\n${errors.joinToString("\n")}")
}
