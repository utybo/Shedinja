package guru.zoroark.shedinja.test

import guru.zoroark.shedinja.ShedinjaException
import guru.zoroark.shedinja.dsl.ShedinjaDsl
import guru.zoroark.shedinja.dsl.put
import guru.zoroark.shedinja.dsl.shedinja
import guru.zoroark.shedinja.environment.EnvironmentBasedScope
import guru.zoroark.shedinja.environment.EnvironmentContext
import guru.zoroark.shedinja.environment.Identifier
import guru.zoroark.shedinja.environment.InjectableModule
import guru.zoroark.shedinja.environment.InjectionEnvironment
import guru.zoroark.shedinja.environment.InjectionEnvironmentKind
import guru.zoroark.shedinja.environment.Injector
import guru.zoroark.shedinja.environment.ScopedContext
import guru.zoroark.shedinja.environment.named
import java.util.Deque
import java.util.LinkedList
import kotlin.reflect.KProperty

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
 * DSL for checking for issues within your Shedinja modules.
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
    checks.forEach { it.check(modules) }
}

private class FakeInjector<T : Any> : Injector<T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        error("Not available")
    }
}

private class DependencyTrackingInjectionEnvironment(context: EnvironmentContext) : InjectionEnvironment {
    companion object : InjectionEnvironmentKind<DependencyTrackingInjectionEnvironment> {
        override fun build(context: EnvironmentContext): DependencyTrackingInjectionEnvironment =
            DependencyTrackingInjectionEnvironment(context)
    }

    override fun <T : Any> getOrNull(identifier: Identifier<T>): T? {
        error("Not available on this kind of environment")
    }

    private val currentInjections = mutableListOf<Identifier<*>>()

    val dependencies = context.declarations.mapValues { (_, v) ->
        currentInjections.clear()
        v.supplier(ScopedContext(EnvironmentBasedScope(this)))
        currentInjections.toList()
    }

    override fun <T : Any> createInjector(identifier: Identifier<T>, onInjection: (T) -> Unit): Injector<T> {
        currentInjections += identifier
        return FakeInjector()
    }
}

/**
 * Checks that no cyclic dependencies are present within the modules.
 *
 * A cyclic dependency is a scenario where a class `Foo` depends on itself, either directly (injects itself into itself)
 * or indirectly (For example, `Foo` depends on `Bar`, which in turn depends on `Foo`).
 *
 * Cyclic dependencies are generally well-supported within Shedinja as long as injections are not done eagerly (see
 * [InjectionEnvironment] for more information). However, they are generally symptoms of badly design systems.
 *
 * Note that this check ignores whether classes are present within a module or not: use the [complete] check for this.
 */
@ShedinjaDsl
val noCycle = IndividualCheck { modules ->
    val env = shedinja(DependencyTrackingInjectionEnvironment) {
        modules.forEach { put(it) }
    }
    // Check for cycles with a simple DFS. Can be optimized to a better algorithm.
    val trace: Deque<Identifier<*>> = LinkedList()
    val visited = mutableSetOf<Pair<Identifier<*>, Identifier<*>>>()

    fun dfs(from: Identifier<*>) {
        if (from in trace) {
            trace.push(from)
            throw ShedinjaCheckException(
                "'noCycle' check failed.\nCyclic dependency found:\n" +
                        trace.reversed().dropWhile { it != from }
                            .joinToString(prefix = "    ", separator = "\n--> ", postfix = "\n") +
                        "Note: --> represents an injection (i.e. A --> B means 'A depends on B')."
            )
        }
        trace.push(from)
        env.dependencies.getValue(from).filter { (from to it) !in visited }.forEach {
            visited.add(from to it)
            dfs(it)
        }
        trace.pop()
    }

    for ((identifier, _) in env.dependencies) {
        trace.clear()
        dfs(identifier)
    }
}

/**
 * Checks that the modules are complete and that all dependencies and injections can be properly resolved.
 *
 * You should almost always use this check within your `shedinjaCheck` block, as the situation of a dependency not being
 * met is rarely beneficial and is usually a sign of a coding error (i.e. forgot to put a dependency in the module
 * definitions, typo in a [named] call...).
 */
@ShedinjaDsl
val complete = IndividualCheck { modules ->
    val env = shedinja(DependencyTrackingInjectionEnvironment) {
        modules.forEach { put(it) }
    }
    val deps = env.dependencies
    val requirementToMissingDependency = deps
        .mapValues { (_, v) -> v.filter { requirement -> !deps.containsKey(requirement) } }
        .filterValues { it.isNotEmpty() }
        .takeIf { it.isNotEmpty() }
    if (requirementToMissingDependency != null) {
        val message = requirementToMissingDependency.asSequence()
            .flatMap { (requester, missingDependencies) -> missingDependencies.map { it to requester } }
            .associateByMultiPair()
            .entries.joinToString(
                prefix =
                """
                    'complete' check failed.
                    Some dependencies were not found. Make sure they are present within your module definitions.
                """.trimIndent() + "\n",
                separator = "\n"
            ) { (k, v) ->
                v.joinToString(
                    prefix = "--> $k not found\n    Requested by:\n", separator = "\n"
                ) { requester -> "    --> $requester" }
            }
        throw ShedinjaCheckException(message)
    }
    val message = deps.mapValues { (_, v) -> v.filter { requirement -> !deps.containsKey(requirement) } }
        .filterValues { it.isNotEmpty() }
        .takeIf { it.isNotEmpty() }
        ?.entries?.joinToString(prefix = "'complete' check failed.\nSome dependencies were not found.\n") { (k, v) ->
            "${v.joinToString()} not found (required by $k)"
        }
    if (message != null) throw ShedinjaCheckException(message)
}

private fun <K, V> Sequence<Pair<K, V>>.associateByMultiPair(): Map<K, List<V>> =
    fold(mutableMapOf<K, MutableList<V>>()) { map, (missing, requester) ->
        map.compute(missing) { _, original ->
            (original ?: mutableListOf()).apply { add(requester) }
        }
        map
    }
