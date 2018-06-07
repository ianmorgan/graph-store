package ianmorgan.docstore

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
import ianmorgan.docstore.mapper.GraphQLMapper
import java.util.HashMap
import kotlin.reflect.KClass


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
                                    DocDataFetcher(docsDao, typeName, helper.objectDefinition(typeName))
                                )
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

                        if (fType is ListType){
                            println ("What to do with a list type query '${f.name}' ??")
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
     * A DataFetcher for a single doc, linked to its DAO. This fetcher is passed the
     * complete ObjectTypeDefinition and also knows how to resolve data for child nodes, which requires
     * recursive calls to the DAOs.
     */
    class DocDataFetcher constructor(docsDao: DocsDao, docName: String, typeDefinition : ObjectTypeDefinition) : DataFetcher<Map<String, Any>?> {
        val dao = docsDao
        val docName = docName
        val typeDefinition = typeDefinition
        override fun get(env: DataFetchingEnvironment): Map<String, Any>? {


            // TODO - what about finding by other fields ???
            val idFieldName = dao.daoForDoc(docName).aggregateKey()
            val id = env.getArgument<String>(idFieldName)
            val data = lookupById(id)

            if (data != null) {
                val helper = Helper.build(typeDefinition)
                for (f in helper.listTypeFieldNames()) {
                    val typeName = helper.typeForField(f)

                    if (typeName == docName) {
                        val ids = data.getOrDefault(f, emptyList<String>()) as List<String>
                        val expanded = ArrayList<Map<String, Any>>()
                        for (theId in ids) {
                            println("internal id is $theId")
                            val x = lookupById(theId)
                            if (x != null) {
                                expanded.add(x)
                            }
                        }
                        // ids replaced with expanded list
                        data.put(f, expanded)
                    }
                }
            }

            return data
        }

        private fun lookupById (id : String) : HashMap<String,Any>? {
            val data = dao.daoForDoc(docName).retrieve(id)
            if (data != null){
                return HashMap(data);
            }
            else {
                return null;
            }
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