package ianmorgan.graphstore.checker;

import graphql.language.*;
import graphql.schema.idl.TypeDefinitionRegistry;
import ianmorgan.graphstore.graphql.Helper;
import ianmorgan.graphstore.graphql.ObjectTypeDefinitionHelper;

import java.util.HashMap;
import java.util.Map;

public class SchemaBuilder {
    private TypeDefinitionRegistry registry;
    private Helper HELPER = Helper.INSTANCE;

    public SchemaBuilder(TypeDefinitionRegistry registry) {
        this.registry = registry;
    }

    public Map<Object, Object> build(String typeName) {
        Map<Object, Object> working = new HashMap<>();

        ObjectTypeDefinition otd = HELPER.build(registry).objectDefinition(typeName);
        for (FieldDefinition field : otd.getFieldDefinitions()) {

            Type rawType = field.getType();

            if (rawType instanceof NonNullType) {
                NonNullType nonNullType = (NonNullType) rawType;

                Type type = nonNullType.getType();
                if (type instanceof TypeName) {
                    Object javaType = graphQLTypeToJsonType(((TypeName) type).getName());
                    working.put(field.getName(), new OneOf(javaType));
                }

                if (type instanceof ListType) {
                    ListType listType = (ListType) type;
                    // this represents a list of enumeration, which we will represent
                    // a list

                    // todo - what if its not an emuration
                    working.put(field.getName(), new OneOf(new ListChecker(String.class)));
                }
            } else {
                if (rawType instanceof ListType) {
                    ListType listType = (ListType) rawType;
                    // this represents a list of enumeration, which we will represent
                    // a list

                    // todo - what if its not an enumeration
                    working.put(field.getName(), new ListChecker(String.class));
                }
                if (rawType instanceof TypeName) {
                    TypeName tName = (TypeName)rawType;
                    if (isScalarType(tName.getName())) {
                        Object javaType = graphQLTypeToJsonType(tName.getName());
                        working.put(field.getName(), javaType);
                    } else {

                        ObjectTypeDefinitionHelper typeHelper = HELPER.buildOTDH(registry,tName.getName());

                        if (typeHelper.hasID()){
                            // linked type
                            working.put(field.getName(), String.class);
                        }
                        else {
                            // embedded type
                            Map<Object, Object> schema = this.build(tName.getName());
                            working.put(field.getName(), new MapChecker(schema));
                        }
                    }
                }
            }
        }

        return working;
    }

    private Object graphQLTypeToJsonType(String typeName) {
        switch (typeName) {

            case "String":
                return String.class;
            case "ID":
                return String.class;
            case "Int":
                return Long.class;
            case "Float":
                return Double.class;
            case "Boolean":
                return Boolean.class;

            default:
                throw new RuntimeException("Don't know about: " + typeName);
        }
    }

    private boolean isScalarType(String typeName) {
        try {
            graphQLTypeToJsonType(typeName);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }


}
