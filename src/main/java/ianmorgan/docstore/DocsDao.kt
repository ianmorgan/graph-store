package ianmorgan.docstore

import graphql.language.ObjectTypeDefinition
import graphql.schema.idl.SchemaParser

/**
 * A Dao to query documents. The real implementation uses an event store,
 * but the client doesn't need to know this.
 */
class DocsDao constructor(graphQLSchema: String) {
    private val daos = HashMap<String, DocDao>();

    init {
        initFromSchema(graphQLSchema)
    }


    val droids = listOf(
        mapOf(
            "id" to "2001", "name" to "R2-D2",
            "appearsIn" to listOf("NEWHOPE", "EMPIRE", "JEDI"), "primaryFunction" to "Astromech"
        ),
        mapOf(
            "id" to "2002", "name" to "C-3PO",
            "appearsIn" to listOf("NEWHOPE", "EMPIRE", "JEDI"), "primaryFunction" to "???"
        )
    )

    val humans = listOf(
        mapOf(
            "id" to "1001", "name" to "Luke Skywalker",
            "appearsIn" to listOf("NEWHOPE", "EMPIRE", "JEDI")
        ),
        mapOf(
            "id" to "1002", "name" to "Princess Leia",
            "appearsIn" to listOf("NEWHOPE", "EMPIRE", "JEDI")
        )
    )

    fun availableDocs () : Set<String>  {
        return daos.keys
    }

    fun daoForDoc(docName : String) : DocDao{
        return  daos[docName]!!
    }

    fun retrieve(aggregateId: String): Map<String, Any> {
        // todo - write some more elagant Kotlin code
        var ret = humans.find { it -> it["id"] == aggregateId }
        if (ret == null) {
            ret = droids.find { it -> it["id"] == aggregateId }
        }
        if (ret != null) return ret
        throw RuntimeException("nothing found !")
    }

    private fun initFromSchema(schema: String) {
        val schemaParser = SchemaParser()
        val typeDefinitionRegistry = schemaParser.parse(schema)

        for (definition in typeDefinitionRegistry.getTypes(ObjectTypeDefinition::class.java)) {
            if (!(definition.name == "Query")) {
                println("Creating dao for ${definition.name}")
                val docName = definition.name
                daos.put(docName, DocDao(definition))
            }
        }
    }
}