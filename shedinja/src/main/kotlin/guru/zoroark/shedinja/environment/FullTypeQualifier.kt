package guru.zoroark.shedinja.environment

import guru.zoroark.shedinja.dsl.ShedinjaDsl
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * A qualifier specifically intended to be used for fully qualifying a type.
 *
 * Because of type erasure in Kotlin, you cannot easily differentiate between a `List<String>` and a `List<Int>`
 * component without using qualifiers. This qualifier is specifically intended to provide a way to differentiate
 * between the two by using their full type encoded as a `KType`.
 *
 * Use the [typed] functions to initialize such qualifiers.
 *
 * @property type The type represented by this qualifier.
 */
@ShedinjaDsl
data class FullTypeQualifier(val type: KType) : Qualifier

/**
 * Creates a full type qualifier with the given [T] as the type.
 */
@ShedinjaDsl
inline fun <reified T : Any> typed(): FullTypeQualifier =
    FullTypeQualifier(typeOf<T>())

/**
 * Creates a full type qualifier with the given KType as the type.
 */
@ShedinjaDsl
fun typed(type: KType): FullTypeQualifier =
    FullTypeQualifier(type)
