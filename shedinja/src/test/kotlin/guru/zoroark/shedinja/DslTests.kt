package guru.zoroark.shedinja

import guru.zoroark.shedinja.dsl.EnvironmentContextBuilderDsl
import guru.zoroark.shedinja.dsl.put
import guru.zoroark.shedinja.environment.Identifier
import guru.zoroark.shedinja.environment.InjectionScope
import guru.zoroark.shedinja.environment.ScopedContext
import guru.zoroark.shedinja.environment.get
import guru.zoroark.shedinja.environment.named
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

class DslTests {
    @Test
    fun `Building from single-element builder works (without warnings)`() {
        val supplier: ScopedContext.() -> ExampleClass = { ExampleClass() }
        val env = EnvironmentContextBuilderDsl().apply {
            put(supplier)
        }
        val built = env.build()
        assertEquals(1, built.declarations.size)
        assertEquals(Identifier(ExampleClass::class), built.declarations.get<ExampleClass>().identifier)
    }

    @Test
    fun `Building from multi-elements builder works`() {
        val supplier: ScopedContext.() -> ExampleClass = { ExampleClass() }
        val supplier2: ScopedContext.() -> ExampleClass2 = { ExampleClass2() }
        val env = EnvironmentContextBuilderDsl().apply {
            put(supplier)
            put(supplier2)
        }
        val built = env.build()
        assertEquals(2, built.declarations.size)
        assertEquals(Identifier(ExampleClass::class), built.declarations.get<ExampleClass>().identifier)
        assertEquals(Identifier(ExampleClass2::class), built.declarations.get<ExampleClass2>().identifier)
    }

    @Test
    fun `Building from constructor references works`() {
        class NoConstructor
        class GoodConstructor(val scope: InjectionScope)

        EnvironmentContextBuilderDsl().apply {
            put(::NoConstructor)
            put(::GoodConstructor)
        }.build()
    }

    @Test
    fun `Duplicate via inferred type put should throw error`() {
        val ex = assertThrows<ShedinjaException> {
            EnvironmentContextBuilderDsl().apply {
                put { ExampleClass() }
                put { ExampleClass() }
            }
        }
        assertEquals(
            "Duplicate identifier: Tried to put 'guru.zoroark.shedinja.ExampleClass (<no qualifier>)', " +
                "but one was already present",
            ex.message
        )
    }

    @Test
    fun `Duplicate via class put should throw error`() {
        val ex = assertThrows<ShedinjaException> {
            EnvironmentContextBuilderDsl().apply {
                put(ExampleClass::class) { ExampleClass() }
                put(ExampleClass::class) { ExampleClass() }
            }
        }
        assertEquals(
            "Duplicate identifier: Tried to put 'guru.zoroark.shedinja.ExampleClass (<no qualifier>)', but " +
                "one was already present",
            ex.message
        )
    }

    @Test
    fun `Duplicate via class and inferred type put should throw error`() {
        val ex = assertThrows<ShedinjaException> {
            EnvironmentContextBuilderDsl().apply {
                put(ExampleClass::class) { ExampleClass() }
                put { ExampleClass() }
            }
        }
        assertEquals(
            "Duplicate identifier: Tried to put 'guru.zoroark.shedinja.ExampleClass (<no qualifier>)', but " +
                "one was already present",
            ex.message
        )
    }

    @Test
    fun `Named and unnamed qualifiers should not throw error`() {
        open class TheSuperclass
        class TheClass : TheSuperclass()

        val context = EnvironmentContextBuilderDsl().apply {
            put(::TheClass)
            put(named("using-ctor"), ::TheClass)
            put(named("using-lambda")) { TheClass() }
            put(TheSuperclass::class, named("using-ctor-and-kclass"), ::TheClass)
            put(TheSuperclass::class, named("using-lambda-and-kclass")) { TheClass() }
        }.build()
        assertEquals(context.declarations.size, 5)
        assertEquals(
            context.declarations.keys,
            setOf(
                Identifier(TheClass::class),
                Identifier(TheClass::class, named("using-ctor")),
                Identifier(TheClass::class, named("using-lambda")),
                Identifier(TheSuperclass::class, named("using-ctor-and-kclass")),
                Identifier(TheSuperclass::class, named("using-lambda-and-kclass"))
            )
        )
    }
}
