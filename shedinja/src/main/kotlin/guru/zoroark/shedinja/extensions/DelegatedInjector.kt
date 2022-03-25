package guru.zoroark.shedinja.extensions

import guru.zoroark.shedinja.environment.Injector
import kotlin.properties.ReadOnlyProperty

/**
 * An injector that delegates the work to another property.
 *
 * This allows you to use [wrapIn] to create injectors, like so:
 *
 * ```
 * DelegatedInjector(something wrapIn { /* do something interesting */ })
 * ```
 */
class DelegatedInjector<T : Any>(property: ReadOnlyProperty<Any?, T>) :
    Injector<T>, ReadOnlyProperty<Any?, T> by property
