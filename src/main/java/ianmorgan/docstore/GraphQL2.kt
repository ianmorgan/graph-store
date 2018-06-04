package ianmorgan.docstore

import graphql.GraphQL
import graphql.Scalars.GraphQLID
import graphql.Scalars.GraphQLString
import graphql.TypeResolutionEnvironment
import graphql.language.InterfaceTypeDefinition
import graphql.language.NonNullType
import graphql.language.Type
import graphql.language.TypeName
import graphql.schema.*
import graphql.schema.GraphQLFieldDefinition.newFieldDefinition
import graphql.schema.idl.RuntimeWiring.newRuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeRuntimeWiring.newTypeWiring


object GraphQLFactory2 {

    fun build(schema: String, docsDao: DocsDao): GraphQL {

        val typeDefinitionRegistry = SchemaParser().parse(schema)
        val helper = GraphQLHelper(typeDefinitionRegistry)

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


        val builder = newRuntimeWiring()

        // wireup handling of query fields
        builder.type("Query",
                { builder ->
                    val queryDefinition = helper.queryDefinition()

                    for (f in queryDefinition.fieldDefinitions) {

                        val typeName = (f.type as TypeName).name
                        val name = f.name

                        if (helper.objectDefinitionNames().contains(typeName)) {
                            // wire up a regular doc fetcher
                            builder.dataFetcher(name, DocDataFetcher(docsDao.daoForDoc(typeName)))
                        } else if (helper.interfaceDefinitionNames().contains(typeName)) {
                            // wire up an Interface data fetcher - this is more complicated
                            // as we need to also understand the interface details (see newTypeWiring
                            // below
                            //
                            // TODO - interfaces need more logic !!
                            builder.dataFetcher(name, DocsDataFetcher(docsDao))
                        } else {
                            println("Don't know what to do with query field $name")
                        }

                    }
                    builder
                }
            )

        // wireup handling of interfaces
        for (name in helper.interfaceDefinitionNames()) {
            builder.type(
                newTypeWiring(name)
                    .typeResolver(InterfaceTypeResolve(helper.interfaceDefinition(name)))
                    .build()
            )
        }

        // build the complete GraphQL object
        val wiring = builder.build()
        val schemaGenerator = SchemaGenerator()
        val graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, wiring)
        val graphQL = GraphQL.newGraphQL(graphQLSchema).build()
        return graphQL
    }

    /**
     * A DataFetcher for a single doc, linked to its DAO
     */
    class DocDataFetcher constructor(docDao: DocDao) : DataFetcher<Map<String, Any>?> {
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
    class NullDataFetcher : DataFetcher<Map<String, Any>?> {
        override fun get(environment: DataFetchingEnvironment?): Map<String, Any>? {
            println("In NullDataFetcher ")
            return emptyMap()
        }
    }

    /**
     * A DataFetcher that just tries all docs. The most basic way of dealing with interfaces
     */
    class DocsDataFetcher constructor(docsDao: DocsDao) : DataFetcher<Map<String, Any>?> {
        val daos = docsDao
        override fun get(env: DataFetchingEnvironment): Map<String, Any>? {
            val id = env.getArgument<String>("id")

            for (doc in daos.availableDocs()) {
                val data = daos.daoForDoc(doc).retrieve(id)
                if (data != null) {
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
    class InterfaceTypeResolve constructor(interfaceDefinition: InterfaceTypeDefinition) : TypeResolver {
        val definition = interfaceDefinition
        override fun getType(env: TypeResolutionEnvironment): GraphQLObjectType {

            println("In InterfaceTypeResolve!! ")
            val builder = GraphQLObjectType.Builder().name("Character")

            for (f in definition.fieldDefinitions) {
                println(f.name)
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
        private fun typeFromType(type: Type<*>): GraphQLScalarType {
            if (type is NonNullType) {
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