package guru.zoroark.shedinja.environment

import guru.zoroark.shedinja.dsl.shedinja
import guru.zoroark.shedinja.entryOf
import guru.zoroark.shedinja.extensions.DeclarationsProcessor
import guru.zoroark.shedinja.extensions.ExtensibleEnvironmentContext
import guru.zoroark.shedinja.extensions.ExtensibleInjectionEnvironment
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

@Suppress("UnnecessaryAbstractClass")
abstract class ExtensibleEnvironmentBaseTest(
    private val provider: (ExtensibleEnvironmentContext) -> ExtensibleInjectionEnvironment
) : EnvironmentBaseTest({ ctx ->
    provider(ExtensibleEnvironmentContext(ctx.declarations, EnvironmentContext(mapOf())))
}) {
    class B
    class A(scope: InjectionScope) {
        val b: B by scope.meta()
    }

    class D
    class E
    class F

    @Test
    fun `(Extension) Injection pass-through to meta-environment`() {
        var a: A? = null
        var b: B? = null
        val context = ExtensibleEnvironmentContext(
            mapOf(
                entryOf {
                    assertNull(a, "Called builder twice!")
                    A(scope).also { a = it }
                }
            ),
            EnvironmentContext(
                mapOf(
                    entryOf {
                        assertNull(b, "Called builder twice!")
                        B().also { b = it }
                    }
                )
            )
        )
        val env = provider(context)
        assertSame(a, env.get())
        assertSame(b, env.metaEnvironment.get())
        assertSame(a?.b, b)
    }

    @Test
    fun `(Extension) getAllIdentifiers`() {
        val ctx = ExtensibleEnvironmentContext(
            mapOf(
                entryOf { D() },
                entryOf(named("E")) { E() },
                entryOf { F() }
            ),
            EnvironmentContext(mapOf())
        )
        val expectedIdentifiers = setOf(
            Identifier(D::class),
            Identifier(E::class, named("E")),
            Identifier(F::class)
        )
        val env = provider(ctx)
        assertEquals(expectedIdentifiers, env.getAllIdentifiers().toSet())
    }

    @Test
    fun `(Extension) Environment injects itself within meta environment`() {
        val env = provider(ExtensibleEnvironmentContext(mapOf(), EnvironmentContext(mapOf())))
        assertSame(env, env.metaEnvironment.get<ExtensibleInjectionEnvironment>())
    }

    @Test
    fun `(Extension) Environment calls declaration processors`() {
        val processed = mutableListOf<Declaration<*>>()
        val processor = object : DeclarationsProcessor {
            override fun processDeclarations(sequence: Sequence<Declaration<*>>) {
                processed.addAll(sequence)
            }
        }
        val env = provider(
            ExtensibleEnvironmentContext(
                mapOf(entryOf { "Hello" }),
                EnvironmentContext(
                    mapOf(entryOf { processor })
                )
            )
        )
        assertEquals(1, processed.size)
        assertEquals(Identifier(String::class), processed[0].identifier)
    }
}
