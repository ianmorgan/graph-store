package ianmorgan.docstore.graphql

import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import ianmorgan.docstore.dal.DocsDao

/**
 * A DataFetcher that just tries all docs. The most basic way of dealing with interfaces
 */
class InterfaceDataFetcher constructor(docsDao: DocsDao) : DataFetcher<Map<String, Any>?> {
    val daos = docsDao
    override fun get(env: DataFetchingEnvironment): Map<String, Any>? {
        val id = env.getArgument<String>("id")

        for (doc in daos.availableDocs()) {
            val data = daos.daoForDoc(doc).retrieve(id)
            if (data != null) {
                return data
            }
        }
        return null;
    }
}