package guru.zoroark.shedinja.environment

class InjectableModule(val name: String, defs: Collection<Declaration<*>>) {
    val declarations: List<Declaration<*>> = defs.toList() // Copy to a list
}
