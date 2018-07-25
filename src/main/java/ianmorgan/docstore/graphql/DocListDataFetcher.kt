package ianmorgan.docstore.graphql

import graphql.language.ObjectTypeDefinition
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import ianmorgan.docstore.dal.DocDao
import ianmorgan.docstore.dal.DocsDao

/**
 * A DataFetcher for a single doc, linked to its DAO. This fetcher is passed the
 * complete ObjectTypeDefinition and also knows how to resolve data for child nodes, which requires
 * recursive calls to the DAOs.
 */
class DocListDataFetcher constructor(docsDao: DocsDao, typeDefinition: ObjectTypeDefinition) :
    DataFetcher<List<Map<String, Any>?>> {
    val dao = docsDao
    val docType = typeDefinition.name
    val typeDefinition = typeDefinition
    override fun get(env: DataFetchingEnvironment): List<Map<String, Any>?> {

        if (env.containsArgument("name")) {
            val name = env.getArgument<String>("name")
            return (dao.daoForDoc(docType) as DocDao).findByField("name", name);
        }

        return emptyList()
    }
}
