package docstore.ianmorgan.github.io

import graphql.language.NonNullType
import graphql.language.ObjectTypeDefinition
import graphql.language.TypeName

/**
 * A Dao for saving (as events) and retrieving a single document. The document structure
 * is controlled by the GraphQL schema
 */
class DocDao constructor(typeDefinition: ObjectTypeDefinition){
    private val definition = typeDefinition
    private val repo = HashMap<String,Map<String,Any>>()
    private lateinit var aggregateKey : String


    init {
        initAggregateKey(typeDefinition)

        if  (aggregateKey == null)  throw RuntimeException("Cannot find an ID field")

    }



    fun store(doc : Map<String,Any>) {

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
}