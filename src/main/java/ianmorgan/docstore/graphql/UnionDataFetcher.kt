package ianmorgan.docstore.graphql

import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import graphql.schema.idl.TypeDefinitionRegistry
import ianmorgan.docstore.dal.DocsDao

/**
 * A DataFetcher that delegates to each type in the union
 */
class UnionDataFetcher constructor(
    docsDao: DocsDao,
    unionName: String,
    registry: TypeDefinitionRegistry
) : DataFetcher<List<Map<String, Any>?>> {
    val daos = docsDao
    val registry = registry
    val unionName = unionName
    override fun get(env: DataFetchingEnvironment): List<Map<String, Any>?> {
        // todo - need to dynamically pull out searchable fields
        val name=   env.getArgument<String>("name_contains")

        val helper = Helper.build(registry)
        val unionType = helper.objectsInUnion(unionName)

        val result = ArrayList<Map<String, Any>?>()

        for (doc in daos.availableDocs()) {
            if (unionType.contains(doc)) {
                val data = daos.daoForDoc(doc).findByField("name_contains",name)
                if (data != null) {
                    for (item in data){
                        val working = HashMap(item)
                        working["#docType"] = doc
                        result.add(working)
                    }
                }
            }
        }
        return result;
    }
}