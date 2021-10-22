package guru.zoroark.shedinja.extensions.external

import guru.zoroark.shedinja.dsl.ShedinjaDsl
import guru.zoroark.shedinja.environment.Qualifier
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * Qualifier that uses full typing information (with generic parameters).
 */
data class FullTypeQualifier(
    /**
     * The actual type.
     */
    val type: KType
) : Qualifier {
    override fun toString(): String = "ofType($type)"
}

/**
 * Creates a full type qualifier from the given reified generic parameter.
 *
 * **Note:** this uses the experimental `typeOf<T>()` function from Kotlin's standard library.
 */
@ShedinjaDsl
@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T> ofType() = FullTypeQualifier(typeOf<T>())

/**
 * Creates a full type qualifier from the given `ktype` parameter.
 */
@ShedinjaDsl
fun ofType(ktype: KType) = FullTypeQualifier(ktype)
