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
import ianmorgan.graphstore.dal.FindResult

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

        val helper = Helper.build(registry)
        val rawArgs = buildArgWalkers(env)

        val findResults =  findMatchingDocs(rawArgs.keys,env.arguments)

        val result = ArrayList<Map<String, Any>>()
        for (row in findResults) {
            val fetcher = DocDataFetcher(daos, helper.objectDefinition(row.docType), registry)
            val walker = rawArgs[row.docType]!!.replaceNodeArgs(mapOf("id" to row.id))
            result.add(fetcher.get(walker)!!)
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

        // Need to emulate the logic in DataFetchingFieldSelectionSetImpl,
        // which doesn't understand how to process Union queries. Basically
        // we treat each fragment of the union query as though it were a standalone
        // query on the individual type.
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
                    argsLookup.put(name, ArgsWalker(df.arguments))
                }
            }
        }
        return argsLookup
    }

    private fun findMatchingDocs(
        unionTypes: Set<String>,
        args : Map<String,Any>
    ): ArrayList<FindResult> {
        val results = ArrayList<FindResult>()
        for (doc in daos.availableDocs()) {
            if (unionTypes.contains(doc)) {
                val data = (daos.daoForDoc(doc) as DocDao).findByFields(args)
                if (data != null) {
                    results.addAll(data)
                }
            }
        }
        return results
    }
}