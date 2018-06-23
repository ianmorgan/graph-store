package ianmorgan.docstore.dal

import graphql.schema.idl.SchemaParser
import ianmorgan.docstore.graphql.Helper
import java.io.File
import java.io.FileInputStream

/**
 * A Dao to query documents. The real implementation uses an event store,
 * but the client doesn't need to know this.
 */
class DocsDao constructor(graphQLSchema: String, eventStoreClient: EventStoreClient = InMemoryEventStore()) {
    private val docDaoLookup = HashMap<String, DocDao>()
    private val interfaceDaoLookup = HashMap<String, InterfaceDao>()
    private val eventStoreClient = eventStoreClient
    private val schema = graphQLSchema


    init {
        initFromSchema(graphQLSchema)
    }


    fun availableDocs(): Set<String> {
        return docDaoLookup.keys
    }

    fun daoForDoc(docName: String): DocDao {
        return docDaoLookup[docName]!!
    }

    fun availableInterfaces(): Set<String> {
        return interfaceDaoLookup.keys
    }

    fun daoForInterface(interfaceName : String) : InterfaceDao {
        return interfaceDaoLookup[interfaceName]!!
    }

    fun schema () : String {
        return schema
    }

    private fun initFromSchema(schema: String) {
        val typeDefinitionRegistry  = SchemaParser().parse(schema)
        val helper = Helper.build(typeDefinitionRegistry)

        // wireup a DocDao for each type
        for (docName in helper.objectDefinitionNames()) {
            docDaoLookup[docName]= DocDao(typeDefinition = helper.objectDefinition(docName),
                eventStoreClient = eventStoreClient)
        }

        // wireup an InterfaceDao for each interface
        for (interfaceName in helper.interfaceDefinitionNames()){
            interfaceDaoLookup.put(interfaceName,
                InterfaceDao(interfaceName, typeDefinitionRegistry, docDaoLookup)
            )
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
