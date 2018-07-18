package ianmorgan.docstore.graphql

import graphql.language.ObjectTypeDefinition
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
        unionName: String,
        typeDefinition: TypeDefinitionRegistry
    ): DataFetcher<List<Map<String, Any>?>> {

        return UnionDataFetcher(docsDao,unionName,typeDefinition)

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