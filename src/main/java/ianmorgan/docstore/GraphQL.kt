package ianmorgan.docstore

import graphql.GraphQL
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import graphql.schema.StaticDataFetcher
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import java.io.File
import java.util.HashMap

import graphql.schema.idl.RuntimeWiring.newRuntimeWiring


object GraphQLFactory {

    fun build() : GraphQL {
        val schema = "type Query{hello: String} schema{query: Query}"


        val schemaFile = File("src/schema/simpsons.graphqls")


        val schemaParser = SchemaParser()
        val typeDefinitionRegistry = schemaParser.parse(schemaFile)


        //typeDefinitionRegistry.merge(schemaParser.parse(schemaFile));

        val simpsonsDataFetcher = object : DataFetcher<Any> {
            override operator fun get(env: DataFetchingEnvironment): Any {

                val name = env.getArgument<String>("name")

                if (name == "homer") {

                    val data = HashMap<Any, Any>()
                    data["name"] = "homer"
                    data["mainCharacter"] = true
                    data["hairColour"] = "bald!"
                    return data
                }
                if (name == "marge") {
                    val data = HashMap<Any, Any>()
                    data["name"] = "marge"
                    data["mainCharacter"] = true
                    data["hairColour"] = "blue"
                    return data
                }

                throw RuntimeException("don't know about $name")
            }
        }

        val runtimeWiring = newRuntimeWiring()
            .type("Query",
                { builder ->
                    builder
                        .dataFetcher("hello", StaticDataFetcher("world"))
                        .dataFetcher("character", simpsonsDataFetcher)
                }
            )

            .build()

        val schemaGenerator = SchemaGenerator()
        val graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring)

        val build = GraphQL.newGraphQL(graphQLSchema).build()
        var executionResult = build.execute("{hello}")
        println(executionResult.getData<Any>().toString())

        val query = "{\n" +
                "  character(name: \"homer\") {\n" +
                "    hairColour\n" +
                "  }\n" +
                "}"

        println(query)

        executionResult = build.execute(query)
        println(executionResult.errors.toString())
        println(executionResult.getData<Any>().toString())
        return build
    }
}