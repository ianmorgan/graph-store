package docstore.ianmorgan.github.io;

import graphql.ExecutionResult;
import graphql.GraphQL;
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

public class HelloWorld {

    public static void main(String[] args) throws Exception {
        String schema = "type Query{hello: String} schema{query: Query}";


        File schemaFile = loadSchema("simpsons.graphqls");


        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schemaFile);


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

        RuntimeWiring runtimeWiring = newRuntimeWiring()
                .type("Query",
                        builder -> builder
                                .dataFetcher("hello", new StaticDataFetcher("world"))
                                .dataFetcher("character", simpsonsDataFetcher)
                                )

                .build();

        SchemaGenerator schemaGenerator = new SchemaGenerator();
        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

        GraphQL build = GraphQL.newGraphQL(graphQLSchema).build();
        ExecutionResult executionResult = build.execute("{hello}");
        System.out.println(executionResult.getData().toString());

        String query = "{\n" +
                "  character(name: \"homer\") {\n" +
                "    hairColour\n" +
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