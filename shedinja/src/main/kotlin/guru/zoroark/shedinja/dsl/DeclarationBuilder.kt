package guru.zoroark.shedinja.dsl

import guru.zoroark.shedinja.environment.ScopedSupplier
import kotlin.reflect.KClass

/**
 * A builder for declarations. This is a simple class that is only responsible for holding a [KClass] and a
 * [ScopedSupplier].
 *
 * @param T The type of the object creation wrapped by this builder
 *
 * @property kclass The class this declaration builder builds into
 * @property supplier The supplier this declaration uses to build the object.
 */
class DeclarationBuilder<T : Any>(val kclass: KClass<T>, val supplier: ScopedSupplier<T>)
