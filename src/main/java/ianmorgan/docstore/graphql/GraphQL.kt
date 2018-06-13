package ianmorgan.docstore.graphql

import graphql.GraphQL
import graphql.Scalars.GraphQLID
import graphql.Scalars.GraphQLString
import graphql.TypeResolutionEnvironment
import graphql.language.*
import graphql.schema.*
import graphql.schema.GraphQLFieldDefinition.newFieldDefinition
import graphql.schema.idl.RuntimeWiring.newRuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeRuntimeWiring.newTypeWiring
import ianmorgan.docstore.dal.DocsDao


object GraphQLFactory2 {

    fun build(schema: String, docsDao: DocsDao): GraphQL {

        val typeDefinitionRegistry = SchemaParser().parse(schema)
        val helper = Helper.build(typeDefinitionRegistry)

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

                    val fType = f.type

                    if (fType is TypeName) {

                        val typeName = fType.name
                        val name = f.name

                        if (helper.objectDefinitionNames().contains(typeName)) {
                            // wire up a regular doc fetcher
                            builder.dataFetcher(
                                name,
                                Fetcher.docFetcher(
                                    docsDao,
                                    helper.objectDefinition(typeName)
                                )
                            )
                        } else if (helper.interfaceDefinitionNames().contains(typeName)) {
                            // wire up an Interface data fetcher - this is more complicated
                            // as we need to also understand the interface details (see newTypeWiring
                            // below
                            //
                            // TODO - interfaces need more logic !!
                            builder.dataFetcher(
                                name,
                                Fetcher.interfaceFetcher(docsDao, null)
                            )
                        } else {
                            println("Don't know what to do with query field $name")
                        }
                    }

                    if (fType is ListType) {

                        val fTypeList = fType.type

                        if (fTypeList is TypeName) {

                            val typeName = fTypeList.name
                            val name = f.name

                            if (helper.objectDefinitionNames().contains(typeName)) {
                                // wire up a regular doc fetcher
                                builder.dataFetcher(
                                    name,
                                    Fetcher.docListFetcher(docsDao, helper.objectDefinition(typeName))
                                )

                            } else if (helper.interfaceDefinitionNames().contains(typeName)) {
                                // wire up an Interface data fetcher - this is more complicated
                                // as we need to also understand the interface details (see newTypeWiring
                                // below
                                //
                                // TODO - interfaces need more logic !!
                                builder.dataFetcher(
                                    name,
                                    DocsDataFetcher(docsDao)
                                )
                            } else {
                                println("Don't know what to do with query field $name")
                            }
                        }


                    }

                }
                builder
            }
        )

        // wireup handling of interfaces
        for (name in helper.interfaceDefinitionNames()) {
            builder.type(
                newTypeWiring(name)
                    .typeResolver(
                        InterfaceTypeResolve(
                            helper.interfaceDefinition(
                                name
                            )
                        )
                    )
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