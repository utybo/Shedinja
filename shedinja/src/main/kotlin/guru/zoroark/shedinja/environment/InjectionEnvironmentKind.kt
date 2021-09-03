package guru.zoroark.shedinja.environment

interface InjectionEnvironmentKind<E : InjectionEnvironment> {
    fun build(context: EnvironmentContext): E
}
