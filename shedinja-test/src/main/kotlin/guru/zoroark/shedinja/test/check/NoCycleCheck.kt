package guru.zoroark.shedinja.test.check

import guru.zoroark.shedinja.dsl.ShedinjaDsl
import guru.zoroark.shedinja.dsl.put
import guru.zoroark.shedinja.dsl.shedinja
import guru.zoroark.shedinja.environment.Identifier
import guru.zoroark.shedinja.environment.InjectionEnvironment
import java.util.Deque
import java.util.LinkedList

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
