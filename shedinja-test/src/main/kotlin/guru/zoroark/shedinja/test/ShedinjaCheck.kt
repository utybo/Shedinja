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
import java.util.*
import kotlin.reflect.KProperty

class ShedinjaCheckException(message: String) : ShedinjaException(message)

fun interface IndividualCheck {
    fun check(modules: List<InjectableModule>)
}

@ShedinjaDsl
class ShedinjaCheckDsl {
    val modules = mutableListOf<InjectableModule>()
    val checks = mutableListOf<IndividualCheck>()
}

@ShedinjaDsl
fun ShedinjaCheckDsl.modules(vararg modules: InjectableModule) {
    this.modules.addAll(modules)
}

@ShedinjaDsl
fun shedinjaCheck(block: ShedinjaCheckDsl.() -> Unit) {
    ShedinjaCheckDsl().apply(block).check()
}

private fun ShedinjaCheckDsl.check() {
    checks.forEach { it.check(modules) }
}

class FakeInjector<T : Any> : Injector<T> {
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

@ShedinjaDsl
val ShedinjaCheckDsl.noCycle: Unit
    get() {
        checks += IndividualCheck { modules ->
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
                        "Cyclic dependency found:\n" +
                                trace.reversed().dropWhile { it != from }
                                    .joinToString(prefix = "    ", separator = "\n--> ", postfix = "\n") +
                                "Note: --> represents an injection (i.e. A --> B means 'A is injected in B')."
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
    }

@ShedinjaDsl
val ShedinjaCheckDsl.complete: Unit
    get() {
        checks += IndividualCheck { modules ->
            val env = shedinja(DependencyTrackingInjectionEnvironment) {
                modules.forEach { put(it) }
            }
            val deps = env.dependencies
            val message =
                deps.mapValues { (_, v) -> v.filter { requirement -> !deps.containsKey(requirement) } }
                    .filterValues { it.isNotEmpty() }
                    .takeIf { it.isNotEmpty() }
                    ?.entries?.joinToString(prefix = "Some dependencies were not found.\n") { (k, v) ->
                        "${v.joinToString()} not found (required by $k)"
                    }
            if (message != null)
                throw ShedinjaCheckException(message)
        }
    }
