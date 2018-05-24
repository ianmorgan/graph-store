package ianmorgan.docstore

import graphql.language.ListType
import graphql.language.NonNullType
import graphql.language.ObjectTypeDefinition
import graphql.language.TypeName
import kotlin.reflect.KClass

/**
 * A Dao for saving (as events) and retrieving a single document. The document structure
 * is controlled by the GraphQL schema.
 */
class DocDao constructor(typeDefinition: ObjectTypeDefinition){
    private val definition = typeDefinition
    private val repo = HashMap<String,Map<String,Any>>()
    private lateinit var aggregateKey : String
    private lateinit var fields : Map<String, KClass<Any>>

    init {
        initAggregateKey(typeDefinition)

        initFields(typeDefinition)

        if  (aggregateKey == null)  throw RuntimeException("Cannot find an ID field to use as the aggregateId")
    }


    fun store(doc : Map<String,Any>) {

    }

    /**
     * Expose the field name to be used as the aggregate key. This is
     * the "ID" field in GraphQL.
     */
    fun aggregateKey () : String {
        return aggregateKey
    }

    /**
     * Expose the fields in the GraphQL schema mapped to their Java types
     *
     * TODO - more work will be needed to define the base set of type conversion rules.
     */
    fun fields () : Map<String,KClass<Any>> {
        return fields
    }


    private fun initAggregateKey(typeDefinition: ObjectTypeDefinition) {
        // navigate the schema information to find an ID field
        for (field in typeDefinition.fieldDefinitions) {
            val rawType = field.type
            if (rawType is NonNullType) {
                val type = rawType.type
                if (type is TypeName) {
                    if (type.name == "ID") {
                        aggregateKey = field.name
                        break
                    }
                }
            }
        }
    }

    private fun initFields(typeDefinition: ObjectTypeDefinition) {
        val working = HashMap<String,KClass<Any>>();

        for (field in typeDefinition.fieldDefinitions) {
            val rawType = field.type
            if (rawType is NonNullType) {
                val type = rawType.type
                if (type is TypeName) {
                    if (type.name == "String") {
                        working[field.name] = String::class as KClass<Any>
                    }
                    if (type.name == "ID") {
                        working[field.name] = String::class as KClass<Any>
                    }
                }
                if (type is ListType){
                    // this represents a list of enumeration, which we will represent
                    // a list
                    working[field.name] = List::class as KClass<Any>
                }
            }
            if (rawType is ListType){
                working[field.name] = List::class as KClass<Any>
            }
            if (rawType is TypeName){
                if (rawType.name == "String") {
                    working[field.name] = String::class as KClass<Any>
                }
                if (rawType.name == "ID") {
                    working[field.name] = String::class as KClass<Any>
                }
            }
        }
        fields = working
    }


}