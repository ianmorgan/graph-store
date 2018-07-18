package ianmorgan.docstore.graphql

import graphql.language.ObjectTypeDefinition
import graphql.language.UnionTypeDefinition
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import graphql.schema.idl.TypeDefinitionRegistry
import graphql.schema.idl.TypeRuntimeWiring
import ianmorgan.docstore.dal.DocsDao
import ianmorgan.docstore.dal.InterfaceDao


/**
 * Does nothing - useful for experimenting and debugging only
 */
class NullDataFetcher : DataFetcher<Map<String, Any>?> {
    override fun get(environment: DataFetchingEnvironment?): Map<String, Any>? {
        println("In NullDataFetcher ")
        return emptyMap()
    }
}

/**
 * Return fixed data - mainly for experimenting and debugging
 */
class FixedDataFetcher constructor(data: Map<String, Any>?) : DataFetcher<Map<String, Any>?> {
    val data = data
    override fun get(environment: DataFetchingEnvironment?): Map<String, Any>? {
        println("In FixedDataFetcher ")
        return data
    }
}

/**
 * Return fixed data - mainly for experimenting and debugging
 */
class FixedListDataFetcher constructor(data: List<Map<String, Any>?>) : DataFetcher<List<Map<String, Any>?>> {
    val data = data
    override fun get(environment: DataFetchingEnvironment?): List<Map<String, Any>?> {
        println("In FixedListDataFetcher ")
        return data
    }
}

class FriendsDataFetcher constructor(dao: InterfaceDao) : DataFetcher<List<Map<String, Any>?>> {
    val dao = dao
    override fun get(environment: DataFetchingEnvironment): List<Map<String, Any>?> {
        println("In FriendsDataFetcher ")

        val result = ArrayList<Map<String, Any>?>()

        val source = environment.getSource<Map<String, Any?>>()

        if (source.containsKey("friends")) {
            for (friendId in source["friends"] as List<String>) {
                val friend = dao.retrieve(friendId)
                if (friend != null) {
                    result.add(friend)
                } else {
                    // todo - this should be adding a warning to the query
                    println("couldnt find friend $friendId")
                }
            }
        }
        return result
    }
}

object Fetcher {

    /**
     * Entry point to fetch for a single doc. Will internally drill down through the query structure until calling
     * other fetchers as necessary, until leaf nodes with scalar values are reached.
     */
    fun docFetcher(
        docsDao: DocsDao,
        typeDefinition: ObjectTypeDefinition,
        builder: TypeRuntimeWiring.Builder
    ): DataFetcher<Map<String, Any>?> {
        return DocDataFetcher(docsDao, typeDefinition, builder)
    }

    /**
     * Entry point to fetch for an interface, picking the correct document by its id. Will internally drill down
     * through the query structure until calling other fetchers as necessary, until leaf nodes with scalar values
     * are reached
     */
    fun interfaceFetcher(
        docsDao: DocsDao,
        interfaceName: String,
        registry: TypeDefinitionRegistry
    ): DataFetcher<Map<String, Any>?> {
        return InterfaceDataFetcher(docsDao, interfaceName, registry)
    }

    /**
     * Entry point to fetch for an interface, picking the correct document by its id. Will internally drill down
     * through the query structure until calling other fetchers as necessary, until leaf nodes with scalar values
     * are reached
     */
    fun unionFetcher(
        docsDao: DocsDao,
        typeDefinition: UnionTypeDefinition?
    ): DataFetcher<List<Map<String, Any>?>> {

        //return DocsDataFetcher(docsDao)
        // hardcoded test data for now
        return FixedListDataFetcher(
            listOf(
                mapOf("#docType" to "Droid", "name" to "RD-D2", "primaryFunction" to "Astromech") as Map<String, Any>
               //mapOf("#docType" to "Human", "name" to "Luke") as Map<String, Any>
            )
        )
    }

    fun docListFetcher(
        docsDao: DocsDao,
        typeDefinition: ObjectTypeDefinition
    ): DataFetcher<List<Map<String, Any>?>> {
        return DocListDataFetcher(docsDao, typeDefinition)
    }

    fun nullDocFetcher(): DataFetcher<Map<String, Any>?> {
        return NullDataFetcher()
    }
}