package guru.zoroark.shedinja.environment

/**
 * An [injection scope][InjectionScope] that delegates the injection to the given environment.
 *
 * This should be used in almost all cases - the ability to control the `inject` call mechanism is mostly useful for
 * dependencies analysis.
 */
class EnvironmentBasedScope(private val env: InjectionEnvironment) : InjectionScope {
    override fun <T : Any> inject(what: Identifier<T>): Injector<T> {
        return env.createInjector(what)
    }
}
