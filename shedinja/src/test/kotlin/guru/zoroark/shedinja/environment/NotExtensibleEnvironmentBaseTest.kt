package guru.zoroark.shedinja.environment

import guru.zoroark.shedinja.entryOf
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

@Suppress("UnnecessaryAbstractClass")
abstract class NotExtensibleEnvironmentBaseTest(
    private val provider: (EnvironmentContext) -> InjectionEnvironment
) : EnvironmentBaseTest(provider) {
    class B
    class A(scope: InjectionScope) {
        val b: B by scope.meta()
    }

    @Test
    fun `(Not extensible) Attempting meta injection should fail`() {
        val context = EnvironmentContext(
            mapOf(
                entryOf {
                    A(scope)
                }
            )
        )
        assertThrows<NotExtensibleException> {
            provider(context)
        }
    }
}
