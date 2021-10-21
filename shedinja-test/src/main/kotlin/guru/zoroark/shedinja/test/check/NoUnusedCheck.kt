package guru.zoroark.shedinja.test.check

import guru.zoroark.shedinja.dsl.ShedinjaDsl
import guru.zoroark.shedinja.dsl.put
import guru.zoroark.shedinja.dsl.shedinja
import guru.zoroark.shedinja.environment.EmptyQualifier
import guru.zoroark.shedinja.environment.Identifier
import guru.zoroark.shedinja.environment.InjectableModule
import guru.zoroark.shedinja.environment.Qualifier
import kotlin.reflect.KClass

private const val noUnusedFooterHelp =
    """
    If some or all of the components mentioned above are still used outside of injections (e.g. via a 'get' call on ^
    the environment), you can exclude them from this rule by adding them after the 'noUnused':
    
        +noUnused {
            exclude<ExcludeThis>()
            exclude<ExcludeThat>(named("exclude.that"))
            exclude(ExcludeIt::class)
            exclude(ExcludeMe::class, named("excluded"))
        }
    """

/**
 * Checks that all the components within modules are injected somewhere, except for the specified in the set passed as
 * a parameter.
 */
class NoUnusedCheck(private val ignoredValues: Set<Identifier<*>>) : IndividualCheck {
    override fun check(modules: List<InjectableModule>) {
        val env = shedinja(DependencyTrackingInjectionEnvironment) {
            modules.forEach { put(it) }
        }
        val dependencies = env.dependencies
        val usedInDependencies = dependencies.values.flatten().toSet()
        val deps = dependencies.keys.filter { it !in usedInDependencies && it !in ignoredValues }
        if (deps.isNotEmpty()) {
            val introLine =
                if (deps.size == 1) "The following component is not injected anywhere, making it unused."
                else "The following components are not injected anywhere, making them unused."
            val message = "'noUnused' check failed.\n" +
                    "$introLine\n" +
                    deps.joinToString(separator = "\n") { "--> $it" } + "\n\n" +
                    noUnusedFooterHelp.trimIndent().replace("^\n", "")
            throw ShedinjaCheckException(message)
        }
    }
}

/**
 * Creates a [NoUnusedCheck] without excluding any class.
 *
 * Note that this check succeeding means that [noCycle] will always fail.
 *
 * You can exclude components from this check using a lambda right after `noUnused`.
 */
@ShedinjaDsl
val noUnused = NoUnusedCheck(setOf())

/**
 * Creates a [NoUnusedCheck], using the block given as a parameter to exclude specific components from this check.
 */
@ShedinjaDsl
inline fun noUnused(block: NoUnusedCheckDsl.() -> Unit): NoUnusedCheck =
    NoUnusedCheckDsl().apply(block).build()

/**
 * DSL receiver for creating a [NoUnusedCheck] with excluded components. Use the [exclude] functions to exclude
 * components by their identifiers.
 */
@ShedinjaDsl
class NoUnusedCheckDsl {
    private val excludes = mutableSetOf<Identifier<*>>()

    /**
     * Excludes a given identifier from the unused check.
     */
    fun exclude(identifier: Identifier<*>) {
        excludes += identifier
    }

    /**
     * Builds a [NoUnusedCheck] based on the exclusions created via the [exclude] functions.
     */
    fun build(): NoUnusedCheck = NoUnusedCheck(excludes.toSet())
}

/**
 * Excludes an identifier built from the generic type parameter and the given qualifier, or [EmptyQualifier] if no
 * qualifier is provided.
 */
@ShedinjaDsl
inline fun <reified T : Any> NoUnusedCheckDsl.exclude(qualifier: Qualifier = EmptyQualifier) {
    exclude(Identifier(T::class, qualifier))
}

/**
 * Excludes an identifier built from the given class and the given qualifier, or [EmptyQualifier] if no qualifier is
 * provided.
 */
@ShedinjaDsl
fun NoUnusedCheckDsl.exclude(kclass: KClass<*>, qualifier: Qualifier = EmptyQualifier) {
    exclude(Identifier(kclass, qualifier))
}
