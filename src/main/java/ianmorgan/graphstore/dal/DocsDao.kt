package ianmorgan.graphstore.dal

import graphql.schema.idl.SchemaParser
import ianmorgan.graphstore.graphql.Helper
import java.io.File
import java.io.FileInputStream

/**
 * Entry point to get hold of DAOs, now a combination of those driven from a GraphQL
 * schema plus those linked to external resources such as REST endpoints.
 *
 * Wiring of dependencies is now a bit convoluted - this is a candidate for some redesign.
 */
class DocsDao constructor(graphQLSchema: String,
                          eventStoreClient: EventStoreClient = InMemoryEventStore(),
                          externalDaos : Map<String,ReaderDao> = HashMap()
) {
    private val externalDaos = externalDaos
    private val availableDocs = HashSet(externalDaos.keys)
    private val docDaoLookup = HashMap(externalDaos)
    private val interfaceDaoLookup = HashMap<String, InterfaceDao>()
    private val eventStoreClient = eventStoreClient
    private val schema = graphQLSchema

    init {
        initFromSchema(graphQLSchema)
    }

    /**
     * All available docs, regardless as to whether they are externally mapped (i.e
     * a DAO has been provided linked to say an external REST api or stored as events
     * in the event store that match the GraphQL schema)
     */
    fun availableDocs(): Set<String> {
        return availableDocs
    }


    fun externalDaos(): Map<String,ReaderDao> {
        return externalDaos
    }


    fun daoForDoc(docType: String): ReaderDao {
        if (docDaoLookup.containsKey(docType)) return docDaoLookup[docType]!!
        if (externalDaos.containsKey(docType)) return externalDaos[docType]!!
        throw RuntimeException("No Dao for $docType")
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
        for (docType in helper.objectDefinitionNames()) {
            // only create a DAO if one hasn't been injected already
            if (!externalDaos.containsKey(docType)) {
                val objectTypeHelper = Helper.build(typeDefinitionRegistry, docType)
                if (objectTypeHelper.idFieldName() != null) {
                    docDaoLookup[docType] = DocDao(
                        typeDefinitionRegistry, docType,
                        eventStoreClient = eventStoreClient
                    )
                    availableDocs.add(docType)
                }
            }
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

