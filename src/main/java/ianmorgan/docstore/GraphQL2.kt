package ianmorgan.docstore

import graphql.GraphQL
import graphql.Scalars.GraphQLID
import graphql.Scalars.GraphQLString
import graphql.TypeResolutionEnvironment
import graphql.language.*
import graphql.schema.*
import graphql.schema.GraphQLFieldDefinition.newFieldDefinition
import graphql.schema.GraphQLInterfaceType.newInterface
import graphql.schema.GraphQLTypeReference.typeRef
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import java.io.File

import graphql.schema.idl.RuntimeWiring.newRuntimeWiring
import graphql.schema.idl.TypeRuntimeWiring.newTypeWiring



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




        val characterInterfaceTypeDefinition = typeDefinitionRegistry.getType("Character", InterfaceTypeDefinition::class.java )


        val runtimeWiring = newRuntimeWiring()
            .type("Query",
                { builder ->
                    val queryDefinition = typeDefinitionRegistry.getType("Query",ObjectTypeDefinition::class.java)

                    for (f in queryDefinition.get().fieldDefinitions){
                        println (f.type)
                        println (f.name)

                        val type = (f.type as TypeName).name
                        val name = f.name

                        if (docsDao.availableDocs().contains(type)){
                            builder.dataFetcher(name,DocDataFetcher(docsDao.daoForDoc(type)))
                        }
                    }

                    builder
                        .dataFetcher("hello", StaticDataFetcher("world"))


                         // todo - should be working this out from the schema
                      //  .dataFetcher("droid", DocDataFetcher(docsDao.daoForDoc("Droid")))
                      //  .dataFetcher("human", DocDataFetcher(docsDao.daoForDoc("Human")))
                        .dataFetcher("character", DocsDataFetcher(docsDao))


                }
            )
            .type(
                // todo - should be working this out from the schema
                newTypeWiring("Character")
                    .typeResolver(InterfaceTypeResolve(characterInterfaceTypeDefinition.get()))
                    .build()
            )
            .build()

        val schemaGenerator = SchemaGenerator()
        val graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring)

        val graphQL = GraphQL.newGraphQL(graphQLSchema).build()
        return graphQL
    }

    /**
     * A DataFetcher for a single doc, linked to its DAO
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

    /**
     * Does nothing - useful for experimenting and debugging only
     */
    class NullDataFetcher : DataFetcher<Map<String,Any>?> {
        override fun get(environment: DataFetchingEnvironment?): Map<String, Any>? {
            println ("In NullDataFetcher ")
            return emptyMap()
        }
    }

    /**
     * A DataFetcher that just tries all docs
     */
    class DocsDataFetcher constructor(docsDao: DocsDao) : DataFetcher<Map<String,Any>?> {
        val daos = docsDao
        override fun get(env: DataFetchingEnvironment): Map<String, Any>? {
            val id = env.getArgument<String>("id")

            for (doc in daos.availableDocs()){
                val data = daos.daoForDoc(doc).retrieve(id)
                if (data != null){
                    return data
                }
            }
            return null;
        }
    }



    /**
     * A TypeResolver for an interface which will figure
     * out which doc to use
     */
    class InterfaceTypeResolve constructor(interfaceDefinition :InterfaceTypeDefinition ): TypeResolver {
        val definition = interfaceDefinition
        override fun getType(env: TypeResolutionEnvironment): GraphQLObjectType {

            println ("In InterfaceTypeResolve!! ")
            val builder =  GraphQLObjectType.Builder().name("Character")

            for (f in definition.fieldDefinitions){
                println (f.name)
                builder.field(
                    newFieldDefinition()
                   .name(f.name)
                    //.description()
                    .type(typeFromType(f.type))
                )
            }
            return builder.build()
        }

        // Take the schema type and convert to one of physical
        // implementation classes
        private fun typeFromType(type : Type<*>) : GraphQLScalarType{
            if (type is NonNullType){
                if (type is TypeName) {
                    if (type.name == "ID") {
                        return GraphQLID
                    }
                }
            }
            return GraphQLString
        }

    }
}