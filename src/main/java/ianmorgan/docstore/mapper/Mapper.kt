package ianmorgan.docstore.mapper

import kotlin.reflect.KClass

/**
 * Encapsulate rules for mapping between GraphQL world and Java/Korlin world
 */
object GraphQLMapper {

    /**
     * The type available in
     *
     */
    fun qLToJsonType (typeName : String) : KClass<Any>{
        return when (typeName) {
            "String" ->  String::class as KClass<Any>
            "ID" ->  String::class as KClass<Any>
            "Int" ->  Int::class as KClass<Any>
            "Float" ->  Double::class as KClass<Any>
            "Boolean" -> Boolean::class as KClass<Any>

            else -> {
                throw RuntimeException ("Don't know about $typeName")
            }
        }

    }

}