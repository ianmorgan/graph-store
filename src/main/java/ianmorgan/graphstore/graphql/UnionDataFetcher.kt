package ianmorgan.graphstore.graphql

import graphql.GraphQLError
import graphql.execution.ExecutionContext
import graphql.execution.ExecutionId
import graphql.execution.ExecutionStrategy
import graphql.execution.ExecutionTypeInfo
import graphql.execution.instrumentation.Instrumentation
import graphql.execution.instrumentation.InstrumentationState
import graphql.language.*
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

        val walker = ArgsWalker(env.selectionSet.arguments)
        println(walker)

        // todo - need to dynamically pull out searchable fields
        val name=   env.getArgument<String>("name_contains")

        val helper = Helper.build(registry)
        val unionType = helper.objectsInUnion(unionName)



        val rawArgs = buildArgWalkers(env)

        val result = ArrayList<Map<String, Any>>()


        findMatchingDocs(unionType, name, result)

        val result2 = ArrayList<Map<String, Any>>()
        for (row in result){
            val docType = row["#docType"] as String
            val id = row["id"] as String

            val fetcher = DocDataFetcher(daos,helper.objectDefinition(docType),registry)

            val args = rawArgs[docType]!!.replaceNodeArgs(mapOf("id" to id))
            result2.add(fetcher.get(args)!!)

        }


        return result2;
    }

    private fun buildArgWalkers(env: DataFetchingEnvironment) : Map<String,ArgsWalker> {
        // need to rebuild enough of an ExecutionContext to access the lower level GraphQLJava APIs.
        val ctx = ExecutionContext( null,
            env.executionId,
            env.graphQLSchema,
            null,
            null,
            null,
            null,
            env.fragmentsByName as (Map<String, FragmentDefinition>),
            null,
            null,
            HashMap<String,Any>(),
            env.getContext<Any>(),
            env.getRoot<Any>())

        val argsLookup = HashMap<String, ArgsWalker>()
        val selectionSet = env.fields[0].selectionSet
        for (selection in selectionSet.selections) {
            if (selection is InlineFragment) {
                val name = (selection.typeCondition as TypeName).name
                println(name)
                println(selection.selectionSet.javaClass.name)

                val type = env.graphQLSchema.typeMap[name]

                //val type = TypeDefinitionRegistryHelper(registry).objectDefinition(name)
                //ExecutionTypeInfo.unwrapBaseType(type)
                if (type is GraphQLFieldsContainer) {
                    val df = DataFetchingFieldSelectionSetImpl.newCollector(
                        ctx,
                        type,
                        env.fields
                    )
                    val args = df.arguments
                    println(args)

                    argsLookup.put(name, ArgsWalker(args))

                }
            }
        }
        return argsLookup
    }

    private fun findMatchingDocs(
        unionType: List<String>,
        name: String,
        result: ArrayList<Map<String, Any>>
    ) {
        for (doc in daos.availableDocs()) {
            if (unionType.contains(doc)) {
                val data = (daos.daoForDoc(doc) as DocDao).findByField("name_contains", name)
                if (data != null) {
                    for (item in data) {
                        val working = mutableMapOf<String, Any>("id" to item["id"]!!)
                        working["#docType"] = doc
                        result.add(working)
                    }
                }
            }
        }
    }
}