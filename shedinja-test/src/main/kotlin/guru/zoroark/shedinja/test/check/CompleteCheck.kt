package guru.zoroark.shedinja.test.check

import guru.zoroark.shedinja.dsl.ShedinjaDsl
import guru.zoroark.shedinja.dsl.put
import guru.zoroark.shedinja.dsl.shedinja
import guru.zoroark.shedinja.environment.named

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
