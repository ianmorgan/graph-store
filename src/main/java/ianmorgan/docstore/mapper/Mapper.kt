package ianmorgan.docstore.mapper

import java.math.BigDecimal
import java.util.*
import kotlin.reflect.KClass

/**
 * Encapsulate rules for mapping between GraphQL world, JSON world and the
 * Java/Kotlin world. This assumes 3 sets of types will be operation.
 *
 * - the types supported by GraphQL
 * - a limited set of Java types (JsonTypes) that can be used directly when
 *   converting to and from JSON. This set tends to the JSON way of thinking,
 *   i.e. a very restricted set of type
 * - a supported set of inbuilt Kotlin / Java types with conversion rules to work
 *   with the above
 *
 * The full implementation should also allow addition of custom mappings
 */

/**
 * The interface that default mapper + any custom mapper need to implement
 *
 *
 */
interface Mapper {

    /**
     * Map the graphQL type to its equivalent 'JavaJson' (i.e. one of
     * the subset of Java types allowed for direct use with a JSON
     * serialization library).
     *
     */
    fun graphQLTypeToJavaJsonType(graphQLType : String) : KClass<Any>

    /**
     * Can the javaJsonObject be converted to the
     */
    fun isJavaJsonObjectGraphQLType(javaJsonObject : Any, graphQLType : String) : Boolean

}



/**
 * Defines the rules for handling an individual type. These assume
 * relative strict type conversion rules but will have enough
 * flexibility to deal with reasonable variations in types (e..g Long and
 * Integer)
 */
interface GraphQLScalarType {
    /**
     * The name in GraphQL
     */
    fun typeName() : String

    /**
     * Can this value be converted to the GraphQL type. The
     * basics of dealing with
     *
     * String('abc') be converted to String
     *
     *
     */
    fun isAssignableFrom(value : Any) : Boolean

    /**
     * The default Java type to be used
     */
    fun defaultJavaType() : KClass<Any>

}



object GraphQLMapper {
    val JSON_TYPES = listOf<KClass<Any>>(
        String::class as KClass<Any>,
        Int::class as KClass<Any>,
        Double::class as KClass<Any>,
        Boolean::class as KClass<Any>,
        List::class as KClass<Any>,
        Map::class as KClass<Any>
    )

    /**
     * Is the supplied type one that is allowed to mapped directly
     * to / from simple JSON. To keep JSON mappings simple this is a very
     * restricted set of types.
     */
    fun supportedJsonType(type: KClass<Any>): Boolean {
        return JSON_TYPES.contains(type)
    }

    /**
     * Take a Java object and convert to one of the allowed 'JsonTypes'.
     * If unknown, convert to a String
     */
    fun javaObjectToJsonType (value : Any) : Any {
        when (value){
            is Date -> { return value.toString()}   // todo - proper date formatting

            else -> {
                return value.toString()
            }
        }
    }


    /**
     * See http://graphql.org/learn/schema
     *
     */
    fun graphQLTypeToJsonType(typeName: String): KClass<Any> {
        return when (typeName) {
            "String" -> String::class as KClass<Any>
            "ID" -> String::class as KClass<Any>
            "Int" -> Long::class as KClass<Any>
            "Float" -> Double::class as KClass<Any>
            "Boolean" -> Boolean::class as KClass<Any>
            else -> {
                //throw RuntimeException("Don't know about $typeName")
                println ("WARNING - Don't know about $typeName");
                String::class as KClass<Any>
            }
        }
    }

    /**
     * Takes the type of class and converts to one of the standarised
     * subset. Mainly this allows for interop with other code and libraries
     * that will chosen different representations of similar concepts, e..g
     * an Array as opposed to a List or an Int as opposed to a Long.
     */
    fun standardiseType(value : Any) : KClass<Any> {
        when (value) {
            is List<*> -> { return List::class as KClass<Any> }
            is Array<*> -> { return List::class as KClass<Any> }
            is Collection<*> -> { return List::class as KClass<Any> }
            is Int -> { return Long::class as KClass<Any>}
            is Float -> { return Double::class as KClass<Any>}
            is BigDecimal -> {
                // make a decision based on the number of decimal places (the scale)
                if (value.scale() == 0)  {
                    return Long::class as KClass<Any>
                }
                else {
                    return Double::class as KClass<Any>
                }
            }
            else -> {
                return value::class as KClass<Any>
            }

            // what about Set? - can they be treated as List ? - probably reasonable in this use case
        }

    }

}