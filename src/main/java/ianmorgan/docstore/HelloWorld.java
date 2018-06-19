package ianmorgan.docstore;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.TypeResolutionEnvironment;
import graphql.language.*;
import graphql.schema.*;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;
import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

public class HelloWorld {

    public static void main(String[] args) throws Exception {
        String schema = "type Query{hello: String} schema{query: Query}";


        File schemaFile = loadSchema("starwars.graphqls");


        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schemaFile);

        for (Map.Entry<String,ObjectTypeDefinition> o : typeDefinitionRegistry.getTypesMap(ObjectTypeDefinition.class).entrySet()){
            System.out.println(o.getKey());
            System.out.println(o.getValue());

        }

        ObjectTypeDefinition type = typeDefinitionRegistry.getType("Droid",ObjectTypeDefinition.class).get();
        for (FieldDefinition fieldDef : type.getFieldDefinitions()){
            if (fieldDef.getType() instanceof NonNullType){
                NonNullType t = (NonNullType)fieldDef.getType();
                TypeName name = (TypeName)t.getType();


            }
        }

        //typeDefinitionRegistry.merge(schemaParser.parse(schemaFile));

        DataFetcher simpsonsDataFetcher = new DataFetcher() {
            @Override
            public Object get(DataFetchingEnvironment env) {

                String name = env.getArgument("name");

                if (name.equals("homer")) {

                    Map data = new HashMap<>();
                    data.put("name", "homer");
                    data.put("mainCharacter", true);
                    data.put("hairColour", "bald!");
                    return data;
                }
                if (name.equals("marge")){
                    Map data = new HashMap<>();
                    data.put("name", "marge");
                    data.put("mainCharacter", true);
                    data.put("hairColour", "blue");
                    return data;
                }

                throw new RuntimeException("don't know about " + name);
            }
        };

        DataFetcher starwarsDataFetcher = new DataFetcher() {
            @Override
            public Object get(DataFetchingEnvironment env) {

                String id = env.getArgument("id");

                if (id.equals("2001")) {

                    Map data = new HashMap<>();
                    data.put("id", id);
                    data.put("name", "R2-D2");
                    return data;
                }
                if (id.equals("2002")){
                    Map data = new HashMap<>();
                    data.put("id", id);
                    data.put("name", "C3PIO");
                    return data;
                }

                throw new RuntimeException("don't know about " + id);
            }
        };

        TypeResolver typeResolver = new TypeResolver() {
            @Override
            public GraphQLObjectType getType(TypeResolutionEnvironment env) {
                Object javaObject = env.getObject();
                    return env.getSchema().getObjectType("Human");
//                } else if (javaObject instanceof Witch) {
//                    return env.getSchema().getObjectType("WitchType");
//                } else {
//                    return env.getSchema().getObjectType("NecromancerType");
//                }
            }
        };

//        RuntimeWiring runtimeWiring = newRuntimeWiring()
//                .type("Query",
//                        builder -> builder
//                                .dataFetcher("hello", new StaticDataFetcher("world"))
//                                .dataFetcher("character", starwarsDataFetcher)
//                                .dataFetcher("droid", starwarsDataFetcher)
//
//                                .typeResolver(typeResolver)
//                                )
//
//                .build();

        RuntimeWiring runtimeWiring =
            RuntimeWiring.newRuntimeWiring()
                    //.scalar(CustomScalar)
                    // this uses builder function lambda syntax
                    .type("QueryType", typeWiring -> typeWiring
                            //.dataFetcher("hero", new StaticDataFetcher(StarWarsData.getArtoo()))
                            .dataFetcher("human", starwarsDataFetcher)
                            .dataFetcher("droid", starwarsDataFetcher)
                    )
                    .type("Human", typeWiring -> typeWiring
                            //.dataFetcher("friends", StarWarsData.getFriendsDataFetcher())
                    )
                    // you can use builder syntax if you don't like the lambda syntax
                    .type("Droid", typeWiring -> typeWiring
                          //  .dataFetcher("friends", StarWarsData.getFriendsDataFetcher())
                    )
                    // or full builder syntax if that takes your fancy
                    .type(
                            newTypeWiring("Character")
                                    .typeResolver(typeResolver)
                                    .build()
                    )
                    .build();


        SchemaGenerator schemaGenerator = new SchemaGenerator();
        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

        GraphQL build = GraphQL.newGraphQL(graphQLSchema).build();
        ExecutionResult executionResult = build.execute("{hello}");
        //System.out.println(executionResult.getData().toString());

        String query = "{\n" +
                "  character(id: \"2002\") {\n" +
                "    name\n" +
                "  }\n" +
                "}";

        System.out.println (query);

        executionResult = build.execute(query);
        System.out.println(executionResult.getErrors().toString());
        System.out.println(executionResult.getData().toString());


        // Prints: {hello=world}
    }

    static File loadSchema(String fileName) throws IOException {
        return new File ("src/schema/" + fileName);
    }
}