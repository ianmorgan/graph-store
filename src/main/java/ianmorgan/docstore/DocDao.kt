package ianmorgan.docstore

import graphql.language.ListType
import graphql.language.NonNullType
import graphql.language.ObjectTypeDefinition
import graphql.language.TypeName
import ianmorgan.docstore.mapper.GraphQLMapper
import kotlin.reflect.KClass
import kotlin.reflect.KFunction2

/**
 * A Dao for saving (as events) and retrieving a single document. The document structure
 * is controlled by the GraphQL schema.
 */
class DocDao constructor(typeDefinition: ObjectTypeDefinition) {
    private val repo = HashMap<String, Map<String, Any>>()
    private lateinit var aggregateKey: String
    private lateinit var fields: Map<String, KClass<Any>>

    init {
        initAggregateKey(typeDefinition)
        initFields(typeDefinition)

        if (aggregateKey == null) throw RuntimeException("Cannot find an ID field to use as the aggregateId")
    }


    /**
     * Store a document. The 'doc' is a simple map of key - value pairs and must match
     * the structure in the schema.
     *
     * See https://ianmorgan.github.io/doc-store/storage for more detail.
     */
    fun store(doc: Map<String, Any>) {
        val id = doc.get(aggregateKey) as String
        if (id != null) {
            checkAgainstSchema(doc)
            repo[id] = doc
        } else {
            throw RuntimeException("must have an aggregate id")
        }
    }

    fun retrieve(aggregateId: String): Map<String, Any>? {
        return repo[aggregateId]
    }

    fun count() : Int{
        return repo.size
    }

    /**
     * Find all docs that match on this field name
     *
     */
    fun findByField(fieldNameExpression: String, value : Any): List<Map<String, Any>> {
        val result = ArrayList<Map<String, Any>>()

        val matcher = pickMatcher(fieldNameExpression)
        val fieldName = rootFieldName(fieldNameExpression)

        // TODO - production quality would need an indexing strategy
        for (doc in repo.values){

            if (matcher(doc[fieldName],value)){
                result.add(doc)
            }
        }
        return result
    }


    fun delete(aggregateId: String) {
        repo.remove(aggregateId)
    }

    /**
     * Expose the field name to be used as the aggregate key. This is
     * the "ID" field in GraphQL.
     */
    fun aggregateKey(): String {
        return aggregateKey
    }

    private fun equalsMatcher (actual : Any?, expected : Any) : Boolean {
        return actual == expected
    }

    private fun containsMatcher (actual : Any?, expected : Any) : Boolean {
        if (actual is String) {
            return actual.toLowerCase().contains((expected as String).toLowerCase())
        }
        else {
            return actual.toString().toLowerCase().contains((expected as String).toLowerCase())
        }
    }

    private fun rootFieldName(fieldNameExpression: String): String {
        return fieldNameExpression.split("_")[0]
    }

    private fun pickMatcher(fieldNameExpression : String): KFunction2<@ParameterName(name = "actual") Any?, @ParameterName(
        name = "expected"
    ) Any, Boolean> {
        if (fieldNameExpression.endsWith("_contains")){
            return ::containsMatcher
        }
        else {
            return ::equalsMatcher
        }
    }

    private fun checkAgainstSchema(doc: Map<String, Any>) {
        // simple implementation for now

        for (key in doc.keys) {
            if (!fields.containsKey(key)) {
                throw RuntimeException("Unexpected field $key in document ")
            }
            // TODO - over simplistic type check
            val expectedType = fields.get(key)!!
            val actual = doc.get(key)
            if (actual != null) {
                if (!(expectedType == GraphQLMapper.standardiseType(actual))) {
                    throw RuntimeException("Types don't match for field $key in document")
                }
            } else {
                println("TODO - should be checking something here")
            }
        }
    }


        /**
         * Expose the fields in the GraphQL schema mapped to their Java types
         *
         * TODO - more work will be needed to define the base set of type conversion rules.
         */
        fun fields(): Map<String, KClass<Any>> {
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
            val working = HashMap<String, KClass<Any>>();

            for (field in typeDefinition.fieldDefinitions) {
                val rawType = field.type
                if (rawType is NonNullType) {
                    val type = rawType.type
                    if (type is TypeName) {
                        working[field.name] = GraphQLMapper.graphQLTypeToJsonType(type.name)
                    }
                    if (type is ListType) {
                        // this represents a list of enumeration, which we will represent
                        // a list
                        working[field.name] = List::class as KClass<Any>
                    }
                }
                if (rawType is ListType) {
                    working[field.name] = List::class as KClass<Any>
                }
                if (rawType is TypeName) {
                    working[field.name] = GraphQLMapper.graphQLTypeToJsonType(rawType.name)
                }
            }
            fields = working
        }


    }