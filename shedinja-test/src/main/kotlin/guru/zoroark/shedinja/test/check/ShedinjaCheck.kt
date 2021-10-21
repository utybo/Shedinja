package guru.zoroark.shedinja.test.check

import guru.zoroark.shedinja.ShedinjaException
import guru.zoroark.shedinja.dsl.ShedinjaDsl
import guru.zoroark.shedinja.environment.InjectableModule

/**
 * Exception type for check failures (i.e. when a check is not met).
 */
class ShedinjaCheckException(message: String) : ShedinjaException(message)

/**
 * An individual check that can be ran.
 */
fun interface IndividualCheck {
    /**
     * Run this check on the given list of modules.
     */
    fun check(modules: List<InjectableModule>)
}

/**
 * DSL receiver class for the Shedinja check DSL.
 */
@ShedinjaDsl
class ShedinjaCheckDsl {
    /**
     * Modules that should be taken into account during the checks
     */
    val modules = mutableListOf<InjectableModule>()

    /**
     * Checks that should be ran. Consider using the [unaryPlus] operator for a more DSLish look.
     */
    val checks = mutableListOf<IndividualCheck>()

    /**
     * Adds this individual check to this Shedinja check instance.
     */
    @ShedinjaDsl
    operator fun IndividualCheck.unaryPlus() {
        checks += this
    }
}

/**
 * Adds the given modules to this Shedinja check instance.
 */
@ShedinjaDsl
fun ShedinjaCheckDsl.modules(vararg modules: InjectableModule) {
    this.modules.addAll(modules)
}

/**
 * DSL for checking your Shedinja modules.
 *
 * Imagine that we have three modules, `web`, `db` and `generate`. A typical use case would look like:
 *
 * ```
 * @Test
 * fun `Shedinja checks`() = shedinjaCheck {
 *     modules(web, db, generate)
 *
 *     +complete
 *     +noCycle
 *     +safeInjection
 * }
 * ```
 *
 * Note that running checks will instantiate the classes within the modules in order to trigger the `by scope()`
 * injections.
 */
@ShedinjaDsl
fun shedinjaCheck(block: ShedinjaCheckDsl.() -> Unit) {
    ShedinjaCheckDsl().apply(block).check()
}

private fun ShedinjaCheckDsl.check() {
    if (checks.isEmpty()) {
        throw ShedinjaCheckException(
            """
            shedinjaCheck called without any rule, which checks nothing.
            --> Add rules using +ruleName (for example '+complete', do not forget the +)
            --> If you do not want to run any checks, remove the shedinjaCheck block entirely.
            For more information, visit https://shedinja.zoroark.guru/ShedinjaCheck
            """.trimIndent()
        )
    } else {
        checks.forEach { it.check(modules) }
    }
}

internal fun <K, V> Sequence<Pair<K, V>>.associateByMultiPair(): Map<K, List<V>> =
    fold(mutableMapOf<K, MutableList<V>>()) { map, (missing, requester) ->
        map.compute(missing) { _, original ->
            (original ?: mutableListOf()).apply { add(requester) }
        }
        map
    }
