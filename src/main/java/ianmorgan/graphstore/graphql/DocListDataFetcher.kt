package ianmorgan.graphstore.graphql

import graphql.language.ObjectTypeDefinition
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import graphql.schema.idl.TypeDefinitionRegistry
import ianmorgan.graphstore.dal.DocDao
import ianmorgan.graphstore.dal.DocsDao

/**
 * A DataFetcher for a single doc, linked to its DAO. This fetcher is passed the
 * complete ObjectTypeDefinition and also knows how to resolve data for child nodes, which requires
 * recursive calls to the DAOs.
 */
class DocListDataFetcher constructor(docsDao: DocsDao, typeDefinition: ObjectTypeDefinition, registry: TypeDefinitionRegistry) :
    DataFetcher<List<Map<String, Any>?>> {
    val dao = docsDao
    val docType = typeDefinition.name
    val typeDefinition = typeDefinition
    val registry = registry
    override fun get(env: DataFetchingEnvironment): List<Map<String, Any>?> {

        val findResults = (dao.daoForDoc(docType) as DocDao).findByFields(env.arguments)

        val result = ArrayList<Map<String, Any>>()
        val rawArgs = ArgsWalker(env.selectionSet.arguments)
        for (row in findResults) {
            val fetcher = DocDataFetcher(dao, typeDefinition, registry)
            val walker = rawArgs.replaceNodeArgs(mapOf("id" to row.id))
            result.add(fetcher.get(walker)!!)
        }

        return result
    }
}
