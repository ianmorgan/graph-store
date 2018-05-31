package ianmorgan.docstore

import graphql.GraphQL
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import graphql.schema.StaticDataFetcher
import graphql.schema.TypeResolver
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import java.io.File
import java.util.HashMap

import graphql.schema.idl.RuntimeWiring.newRuntimeWiring
import graphql.schema.idl.TypeRuntimeWiring.newTypeWiring


object GraphQLFactory2 {

    fun build(schema: String, docsDao : DocsDao) : GraphQL {


        val schemaParser = SchemaParser()
        val typeDefinitionRegistry = schemaParser.parse(schema)


        //typeDefinitionRegistry.merge(schemaParser.parse(schemaFile));

        val singleDocDataFetcher = object : DataFetcher<Any> {
            override operator fun get(env: DataFetchingEnvironment): Any {

                val id = env.getArgument<String>("id")
                val data = docsDao.daoForDoc("Droid").retrieve(id)
                return data
            }
        }

        val runtimeWiring = newRuntimeWiring()
            .type("Query",
                { builder ->
                    builder
                        .dataFetcher("hello", StaticDataFetcher("world"))
                        .dataFetcher("droid", singleDocDataFetcher)
                }
            )
            .type(
                newTypeWiring("Character")
                   // .typeResolver({  })
                    .build()
            )


            .build()

        val schemaGenerator = SchemaGenerator()
        val graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring)

        val build = GraphQL.newGraphQL(graphQLSchema).build()
        //var executionResult = build.execute("{hello}")
        //println(executionResult.getData<Any>().toString())

        val query = "{\n" +
                "  droid(id: \"2001\") {\n" +
                "    name\n" +
                "  }\n" +
                "}"

        println(query)

        val executionResult = build.execute(query)
        println(executionResult.errors.toString())
        println(executionResult.getData<Any>().toString())
        return build
    }
}