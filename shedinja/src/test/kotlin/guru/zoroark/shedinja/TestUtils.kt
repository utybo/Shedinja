package guru.zoroark.shedinja

import guru.zoroark.shedinja.environment.Declaration
import guru.zoroark.shedinja.environment.EmptyQualifier
import guru.zoroark.shedinja.environment.Identifier
import guru.zoroark.shedinja.environment.Qualifier
import guru.zoroark.shedinja.environment.ScopedSupplier

inline fun <reified T : Any> entryOf(qualifier: Qualifier = EmptyQualifier, noinline supplier: ScopedSupplier<T>) =
    Declaration(Identifier(T::class, qualifier), supplier).let {
        it.identifier to it
    }
