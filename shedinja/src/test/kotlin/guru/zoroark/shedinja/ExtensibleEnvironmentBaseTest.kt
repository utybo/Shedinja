package guru.zoroark.shedinja

import guru.zoroark.shedinja.environment.EnvironmentContext
import guru.zoroark.shedinja.environment.Identifier
import guru.zoroark.shedinja.environment.InjectionScope
import guru.zoroark.shedinja.environment.get
import guru.zoroark.shedinja.environment.invoke
import guru.zoroark.shedinja.environment.named
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
}
