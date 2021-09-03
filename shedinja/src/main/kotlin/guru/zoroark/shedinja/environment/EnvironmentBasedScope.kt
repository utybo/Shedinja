package guru.zoroark.shedinja.environment

class EnvironmentBasedScope(private val env: InjectionEnvironment) : InjectionScope {
    override fun <T : Any> inject(what: Identifier<T>): Injector<T> {
        return env.createInjector(what)
    }
}
