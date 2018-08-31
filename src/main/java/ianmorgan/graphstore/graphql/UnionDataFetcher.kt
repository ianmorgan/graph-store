package ianmorgan.graphstore.graphql

import graphql.execution.ExecutionContext
import graphql.language.FragmentDefinition
import graphql.language.InlineFragment
import graphql.language.TypeName
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import graphql.schema.DataFetchingFieldSelectionSetImpl
import graphql.schema.GraphQLFieldsContainer
import graphql.schema.idl.TypeDefinitionRegistry
import ianmorgan.graphstore.dal.DocDao
import ianmorgan.graphstore.dal.DocsDao

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
        val name = env.getArgument<String>("name_contains")

        val helper = Helper.build(registry)
        val findResult = findMatchingDocs(helper.objectsInUnion(unionName), name)

        val rawArgs = buildArgWalkers(env)
        val result = ArrayList<Map<String, Any>>()
        for (row in findResult) {
            val docType = row["#docType"] as String
            val id = row["id"] as String

            // find might find results that aren't of a type expected in the union query
            if (rawArgs.containsKey(docType)) {
                val fetcher = DocDataFetcher(daos, helper.objectDefinition(docType), registry)
                val walker = rawArgs[docType]!!.replaceNodeArgs(mapOf("id" to id))
                result.add(fetcher.get(walker)!!)
            }

        }

        return result;
    }

    private fun buildArgWalkers(env: DataFetchingEnvironment): Map<String, ArgsWalker> {
        // need to rebuild enough of an ExecutionContext to access the lower level GraphQLJava APIs.
        val ctx = ExecutionContext(
            null,
            env.executionId,
            env.graphQLSchema,
            null,
            null,
            null,
            null,
            env.fragmentsByName as (Map<String, FragmentDefinition>),
            null,
            null,
            HashMap<String, Any>(),
            env.getContext<Any>(),
            env.getRoot<Any>()
        )

        val argsLookup = HashMap<String, ArgsWalker>()
        val selectionSet = env.fields[0].selectionSet

        // Need to emulate the logic in DataFetchingFieldSelectionSetImpl
        // which doesn't understand how to process Union queries. Basically
        // we treat each fragment of the query as though it were a standalone
        // query on the individual types
        for (selection in selectionSet.selections) {
            if (selection is InlineFragment) {
                val name = (selection.typeCondition as TypeName).name

                val type = env.graphQLSchema.typeMap[name]

                if (type is GraphQLFieldsContainer) {
                    val df = DataFetchingFieldSelectionSetImpl.newCollector(
                        ctx,
                        type,
                        env.fields
                    )
                    val args = df.arguments

                    argsLookup.put(name, ArgsWalker(args))
                }
            }
        }
        return argsLookup
    }

    private fun findMatchingDocs(
        unionType: List<String>,
        name: String
    ): ArrayList<Map<String, Any>> {
        val results = ArrayList<Map<String, Any>>()
        for (doc in daos.availableDocs()) {
            if (unionType.contains(doc)) {
                val data = (daos.daoForDoc(doc) as DocDao).findByField("name_contains", name)
                if (data != null) {
                    for (item in data) {
                        val working = mutableMapOf<String, Any>("id" to item["id"]!!)
                        working["#docType"] = doc
                        results.add(working)
                    }
                }
            }
        }
        return results
    }
}