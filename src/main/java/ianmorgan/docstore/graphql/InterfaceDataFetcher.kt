package ianmorgan.docstore.graphql

import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import graphql.schema.idl.TypeDefinitionRegistry
import ianmorgan.docstore.dal.DocsDao

/**
 * A DataFetcher that just tries all docs. The most basic way of dealing with interfaces
 */
class InterfaceDataFetcher constructor(
    docsDao: DocsDao,
    interfaceName: String,
    registry: TypeDefinitionRegistry
) : DataFetcher<Map<String, Any>?> {
    val daos = docsDao
    val registry = registry
    val interfaceName = interfaceName
    override fun get(env: DataFetchingEnvironment): Map<String, Any>? {
        val id = env.getArgument<String>("id")

        val helper = Helper.build(registry)
        val implementingTypes = helper.objectsImplementingInterface(interfaceName);

        for (doc in daos.availableDocs()) {
            if (implementingTypes.contains(doc)) {
                val data = daos.daoForDoc(doc).retrieve(id)
                if (data != null) {
                    return data
                }
            }
        }
        return null;
    }
}