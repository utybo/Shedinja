package guru.zoroark.shedinja.environment

import guru.zoroark.shedinja.dsl.ShedinjaDsl

/**
 * An object that contains a scope. For use as a receiver in lambdas (e.g. [ScopedSupplier])
 */
@ShedinjaDsl
interface ScopedContext {
    /**
     * A scope, represented as an [SComponent]. This scope can be used to perform injection.
     */
    val scope: SComponent
}

private class SimpleScopedContext(override val scope: SComponent): ScopedContext

/**
 * Creates a `ScopedContext` that contains the given scope as-is as a property.
 */
fun ScopedContext(scope: SComponent): ScopedContext = SimpleScopedContext(scope)

/**
 * A supplier of T that takes a [ScopedContext] as a receiver.
 */
typealias ScopedSupplier<T> = ScopedContext.() -> T
