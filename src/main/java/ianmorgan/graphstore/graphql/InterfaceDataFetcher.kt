package ianmorgan.graphstore.graphql

import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import graphql.schema.idl.TypeDefinitionRegistry
import ianmorgan.graphstore.dal.DocsDao

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

    /**
     * Entrypoint when called recursively inside a query (i.e. for nested data). For simplicity of
     * wiring these bypass the GraphQLJava api and simply pass on the query args (see ArgsWalker),
     * which has all the information in the original query.
     */
    fun get(params: Map<String,Any>): Map<String, Any>? {
        val id = params["id"] as String

        val helper = Helper.build(registry)
        val implementingTypes = helper.objectsImplementingInterface(interfaceName);

        for (doc in daos.availableDocs()) {
            if (implementingTypes.contains(doc)) {
                val data = daos.daoForDoc(doc).retrieve(id)
                if (data != null) {
                    data.put("#docType", doc)
                    return data
                }
            }
        }
        return null;
    }

    /**
     * Entry point when called by GraphQLJava API.
     */
    override fun get(env: DataFetchingEnvironment): Map<String, Any>? {
        val id = env.getArgument<String>("id")

        val helper = Helper.build(registry)
        val implementingTypes = helper.objectsImplementingInterface(interfaceName);

        for (doc in daos.availableDocs()) {
            if (implementingTypes.contains(doc)) {
                val data = daos.daoForDoc(doc).retrieve(id)
                if (data != null) {
                    data.put("#docType", doc)
                    return data
                }
            }
        }
        return null;
    }
}