package ianmorgan.docstore

import graphql.GraphQL
import graphql.Scalars.GraphQLString
import graphql.schema.*
import graphql.schema.GraphQLFieldDefinition.newFieldDefinition
import graphql.schema.GraphQLInterfaceType.newInterface
import graphql.schema.GraphQLTypeReference.typeRef
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import java.io.File
import java.util.HashMap

import graphql.schema.idl.RuntimeWiring.newRuntimeWiring
import graphql.schema.idl.TypeRuntimeWiring.newTypeWiring
import java.util.Collections.list
import java.util.Objects.nonNull


object GraphQLFactory2 {

    fun build(schema: String, docsDao : DocsDao) : GraphQL {


        val schemaParser = SchemaParser()
        val typeDefinitionRegistry = schemaParser.parse(schema)

//         val typeResovler = TypeResolver() {
//            @Override
//            GraphQLObjectType getType(TypeResolutionEnvironment env) {
//                def id = env.getObject().id
//                if (humanData[id] != null)
//                    return StarWarsSchema.humanType
//                if (droidData[id] != null)
//                    return StarWarsSchema.droidType
//                return null
//            }
//        }

//        val typeReolver = TypeResolver(function = { env ->
//
//
//        })

//        var characterInterface = newInterface()
//            .name("Character")
//            .description("A character in the Star Wars Trilogy")
//            .field(
//                newFieldDefinition()
//                    .name("id")
//                    .description("The id of the character.")
//                    //.type(nonNull(GraphQLString))
//            )
//            .field(
//                newFieldDefinition()
//                    .name("name")
//                    .description("The name of the character.")
//                    .type(GraphQLString)
//            )
//            .field(
//                newFieldDefinition()
//                    .name("friends")
//                    .description("The friends of the character, or an empty list if they have none.")
//                    //.type(list(typeRef("Character")))
//            )
//            .field(
//                newFieldDefinition()
//                    .name("appearsIn")
//                    .description("Which movies they appear in.")
//                    //.type(list(episodeEnum))
//            )
//          //  .typeResolver(StarWarsData.getCharacterTypeResolver())
//            .build()


        //typeDefinitionRegistry.merge(schemaParser.parse(schemaFile));


        val runtimeWiring = newRuntimeWiring()
            .type("Query",
                { builder ->
                    builder
                        .dataFetcher("hello", StaticDataFetcher("world"))

                         // todo - should be working this out from the schema
                        .dataFetcher("droid", DocDataFetcher(docsDao.daoForDoc("Droid")))
                        .dataFetcher("human", DocDataFetcher(docsDao.daoForDoc("Human")))

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

    /**
     * A Data fetcher for a single doc, linked to its DAO
     */
    class DocDataFetcher constructor(docDao: DocDao) : DataFetcher<Map<String,Any>?> {
        val dao = docDao
        override fun get(env: DataFetchingEnvironment): Map<String, Any>? {
            // TODO - what about finding by other fields ???
            val id = env.getArgument<String>("id")
            val data = dao.retrieve(id)
            return data
        }

    }
}