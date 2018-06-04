package ianmorgan.docstore

import graphql.language.ObjectTypeDefinition
import graphql.schema.idl.SchemaParser
import java.io.File
import java.io.FileInputStream

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

    fun availableDocs(): Set<String> {
        return daos.keys
    }

    fun daoForDoc(docName: String): DocDao {
        return daos[docName]!!
    }

    fun retrieve(aggregateId: String): Map<String, Any> {
        // todo - write some more elegant Kotlin code!
        var ret = humans.find { it -> it["id"] == aggregateId }
        if (ret == null) {
            ret = droids.find { it -> it["id"] == aggregateId }
        }
        if (ret != null) return ret
        throw RuntimeException("nothing found !")
    }

    private fun initFromSchema(schema: String) {
        val helper = GraphQLHelper(SchemaParser().parse(schema))
        for (docName in helper.objectDefinitionNames()) {
            daos.put(docName, DocDao(helper.objectDefinition(docName)))
        }
    }

    companion object {
        fun fromSchema(schema: String): DocsDao {
            return DocsDao(schema)
        }

        fun fromSchema(schemaFile: File): DocsDao {
            val schemaString = FileInputStream(schemaFile).bufferedReader().use { it.readText() }  // defaults to UTF-8
            return DocsDao(schemaString)
        }
    }
}
