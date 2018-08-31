package ianmorgan.graphstore.graphql

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
import graphql.schema.idl.TypeDefinitionRegistry
import graphql.schema.idl.TypeRuntimeWiring.newTypeWiring
import ianmorgan.graphstore.dal.DocsDao


object GraphQLFactory {

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
                                    helper.objectDefinition(typeName),
                                    typeDefinitionRegistry
                                )
                            )
                        } else if (helper.interfaceDefinitionNames().contains(typeName)) {

                            builder.dataFetcher(
                                name,
                                Fetcher.interfaceFetcher(docsDao,
                                    typeName,
                                    typeDefinitionRegistry)
                            )
                        } else if (helper.unionDefinitionNames().contains(typeName)) {

                            builder.dataFetcher(
                                name,
                                Fetcher.unionFetcher(docsDao, typeName, typeDefinitionRegistry)
                            )
                        }
                        else {
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
                                    Fetcher.docListFetcher(docsDao, helper.objectDefinition(typeName),typeDefinitionRegistry)
                                )

                            } else if (helper.interfaceDefinitionNames().contains(typeName)) {
                                // wire up an Interface data fetcher - this is more complicated
                                // as we need to also understand the interface details (see newTypeWiring
                                // below
                                //
                                // TODO - interfaces need more logic !!
                                builder.dataFetcher(
                                    name,
                                    InterfaceDataFetcher(docsDao, name, typeDefinitionRegistry)
                                )
                            } else if (helper.unionDefinitionNames().contains(typeName)) {

                                builder.dataFetcher(
                                    name,
                                    Fetcher.unionFetcher(docsDao, typeName, typeDefinitionRegistry)
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

//        builder.type("Human", { builder ->
//            builder.
//            )

       // builder.type("Human", typeWiring -> type

        // TODO - this should be built dynamiaclly from the shchema
//        builder.type("Droid", { builder ->
//            builder.dataFetcher("friends", FriendsDataFetcher(docsDao.daoForInterface("Character")))
//            builder
//        })
//
//        builder.type("Human", { builder ->
//            builder.dataFetcher("friends", FriendsDataFetcher(docsDao.daoForInterface("Character")))
//            builder
//        })


        // wireup handling of interfaces
        for (name in helper.interfaceDefinitionNames()) {
            builder.type(
                newTypeWiring(name)
                    .typeResolver(
                        InterfaceTypeResolve(helper.interfaceDefinition(name))
                    )
                    .build()
            )
        }

        // wireup handling of union types
        for (name in helper.unionDefinitionNames()) {
            builder.type(
                newTypeWiring(name)
                    .typeResolver(UnionTypeResolve(typeDefinitionRegistry,name)
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

            val name = definition.name
            val builder = GraphQLObjectType.Builder().name(name)

            for (f in definition.fieldDefinitions) {
                builder.field(
                    newFieldDefinition()
                        .name(f.name)
                        //.description()
                        .type(typeFromType(f.type,this))
                )
            }
            return builder.build()
        }

//        override fun getType(env: TypeResolutionEnvironment): GraphQLObjectType {
//
//            val data = env.getObject<Map<String,Any>>()
//            val name = data["#docType"]!! as String
//            val objectType = env.getSchema().getObjectType(name);
//
//            return objectType;
//
//        }

        // Take the schema type and convert to one of physical
        // implementation classes
        private fun typeFromType(type: Type<*>, res: InterfaceTypeResolve): GraphQLOutputType {
            if (type is NonNullType) {
                val t = type.type
                if (t is TypeName) {
                    if (t.name == "ID") {
                        return GraphQLID
                    }

                }
                if (t is ListType) {
                    val tt = t.type
                    println (tt)
//                if (t is TypeName) {
//                    if (t.name == "ID") {
//                        return GraphQLID
//                    }
//                }
                    return GraphQLList(GraphQLString)
                }
            }
            if (type is ListType){
                val t = type.type
                println (t)
                if (t is TypeName){
                    println ("its an interface!")

                    if (t.name == "Character") {
                        var x = GraphQLInterfaceType.Builder()
                            .name("Character")
                            .description("wibble")
                            .typeResolver(res)
                            .build()

                        // todo - need to wiring in an interface handler
                        return GraphQLList(x)
                    }
                }
//                if (t is TypeName) {
//                    if (t.name == "ID") {
//                        return GraphQLID
//                    }
//                }
                return GraphQLList (GraphQLString)
            }
            else {

            }
            return GraphQLString
        }

    }


    /**
     * A TypeResolver for an union which will figure
     * out which doc to use
     */
    class UnionTypeResolve constructor(typeDefinitionRegistry: TypeDefinitionRegistry, unionName : String) : TypeResolver {
        val typeDefinitionRegistry = typeDefinitionRegistry
        val unionName = unionName
        val helper = TypeDefinitionRegistryHelper(typeDefinitionRegistry)
        override fun getType(env: TypeResolutionEnvironment): GraphQLObjectType {

            val data = env.getObject<Map<String,Any>>()
            val name = data["#docType"]!! as String
            val objectType = env.getSchema().getObjectType(name);

            return objectType;

        }
    }
}