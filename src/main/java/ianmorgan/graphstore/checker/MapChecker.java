package ianmorgan.graphstore.checker;

import java.util.*;

/**
 * Allows the specification of basic schema rules for a message or
 * data structure as a set of key / values.
 * <p>
 * This is generally used to:
 * - validate input messages against a scheme before accepting them
 * - meta data for automated view generation
 * <p>
 * Slightly inspired by the schema specification & validation approach of GraphQL (http://graphql.org/).
 */
public class MapChecker {

    private Map<Object, Object> schema;

    public MapChecker(Map<Object, Object> schema) {
        this.schema = schema;
    }


    /**
     * Run all validations and return false if there are any problems.
     *
     * @param input
     * @return
     */
    public boolean isValid(Map<String, Object> input) {
        return validate(input).isEmpty();
    }


    /**
     * Run all validations and return the full list of problems
     *
     * @param input
     * @return
     */
    public List<String> validate(Map<String, Object> input) {
        if (input.isEmpty()) return Arrays.asList("Cannot validate an empty map");

        Context ctx = new Context();
        validate(ctx, input);
        return ctx.failures;
    }

    public List<String> validate(Context ctx, Map<String, Object> input) {
        Set<Object> allKeys = new HashSet<>(schema.keySet());
        for (Map.Entry<String, Object> item : input.entrySet()) {

            // mandatory field
            if (lookupInSchema(item) instanceof Relationship) {

                Relationship expected = (Relationship) lookupInSchema(item);
                ctx.nesting.add(new Node(NodeType.Type, item.getKey(), item.getValue()));
                validateNotNull(ctx, item.getValue());

                if (expected instanceof OneOf) {
                    if (expected.theType() instanceof ListChecker) {
                        if (item.getValue() != null) {
                            validateList(ctx, allKeys,
                                    item.getKey(),
                                    item.getValue(),
                                    (ListChecker) expected.theType());
                        }
                    } else {
                        validateType(ctx, item.getValue(), (Class) expected.theType());
                    }
                }
                allKeys.remove(ctx.nesting.pop().schemaKey);
            }

            // optional field
            if (!(lookupInSchema(item) instanceof Relationship)) {
                Object expected = lookupInSchema(item);

                ctx.nesting.add(new Node(NodeType.Type, lookupKeySchema(item), item.getKey(), item.getValue()));

                if (expected instanceof ListChecker) {
                    if (item.getValue() != null) {
                        validateList(ctx, allKeys,
                                item.getKey(),
                                item.getValue(),
                                (ListChecker) expected);
                    }
                } else if (expected instanceof MapChecker) {
                    if (item.getValue() != null) {
                        MapChecker checker = (MapChecker) expected;
                        if (item.getValue() instanceof Map) {
                            checker.validate(ctx, (Map) item.getValue());
                            allKeys.remove(item.getKey());

                        } else {
                            if (item.getValue() != null) {
                                ctx.failures.add(printNesting(ctx.nesting, item.getKey()) + " is not a  map");
                            }
                            allKeys.remove(item.getKey());
                        }
                    }
                } else {
                    validateType(ctx, item.getValue(), (Class) expected);
                }
                allKeys.remove(ctx.nesting.pop().name);
            }

            // unexpected field
            if (lookupInSchema(item) == null) {
                ctx.failures.add(printNesting(ctx.nesting, item.getKey()) + " is not in the schema");
            }
        }

        for (Object key : allKeys) {
            if ((schema.get(key) instanceof Relationship)) {
                ctx.failures.add(printNesting(ctx.nesting, key) + " is mandatory");
            }
        }

        return ctx.failures;
    }

    private Object lookupInSchema(Map.Entry<String, Object> item) {

        Object result = schema.get(item.getKey());
        if (result == null) {
            result = lookupExpectedByEnum(item.getKey());
        }
        if (result == null) {
            result = lookupByUUID(item.getKey());
        }
        return result;
    }


    /**
     * Figure out what the actual schemaKey type is in the scheme, String, Enum or UUID
     *
     * @param item
     * @return
     */
    private Class lookupKeySchema(Map.Entry<String, Object> item) {

        Object result = schema.get(item.getKey());
        if (result instanceof String){
            return String.class;
        }

        Class clazz = lookupKeyByEnum(item.getKey());
        if (clazz != null) {
            return clazz;
        }
        // TODO - logic for UUID
//        if (result == null) {
//            result = lookupByUUID(item.getKey());
//        }
        return null;
    }

    private Object lookupExpectedByEnum(Object key) {
        for (Object item : schema.keySet()) {
            if (item instanceof Class) {
                if (((Class) item).isEnum()) {
                    for (Enum e : ((Class<Enum>) item).getEnumConstants()) {
                        if (e.name() == key) {
                            return schema.get(item);
                        }
                    }
                }
            }
        }
        return null;
    }

    private Class<Enum> lookupKeyByEnum(Object key) {
        for (Object item : schema.keySet()) {
            if (item instanceof Class) {
                if (((Class) item).isEnum()) {
                    for (Enum e : ((Class<Enum>) item).getEnumConstants()) {
                        if (e.name() == key) {
                            return (Class)item;
                        }
                    }
                }
            }
        }
        return null;
    }

    private Object lookupByUUID(Object key) {
        // is the supplied key a UUID, and does the checker support UUID keys

        for (Object item : schema.keySet()) {
            if (item instanceof Class) {
                if (((Class) item).equals(UUID.class)) {
                    try {
                        UUID.fromString((String) key);
                        return schema.get(item);
                    } catch (IllegalArgumentException iae) {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    private void validateList(Context ctx, Set<Object> allKeys, Object key, Object value, ListChecker checker) {
        if (value instanceof List) {
            checker.validate(ctx, (List) value);
        } else {
            if (value != null) {
                ctx.failures.add(printNesting(ctx.nesting) + " : expected a list, but found a '" + value.getClass() + "'");
            }
        }
        allKeys.remove(key);

    }


    private void validateType(Context ctx, Object item, Class expectedClazz) {
        if (expectedClazz == null) {
            System.out.println("Ignoring as no expectedClazz - need to cleanup logic");
            return;
        }

        if (expectedClazz.isEnum()) {
            if (item != null) {
                try {
                    // try and build the enum
                    Enum.valueOf((Class<? extends Enum>) expectedClazz, (String) item);

                } catch (RuntimeException ex) {
                    ctx.failures.add(printNesting(ctx.nesting) + " : " + item + " is not a valid " + expectedClazz.getSimpleName() + " enum");
                }
            }
        } else {
            if (item != null) {
                if (!TypeCheckers.check(item, expectedClazz)) {
                    ctx.failures.add(printNesting(ctx.nesting) + " : " + item + " is not a " + expectedClazz.getSimpleName());
                }
            }
        }
    }

    private void validateNotNull(Context ctx, Object item) {
        if (item == null) {
            ctx.failures.add(printNesting(ctx.nesting) + " : is mandatory and null is not allowed");
        }
    }


    private String printNesting(List<Node> nesting, String nodeName) {
        StringBuilder sb = new StringBuilder(printNesting(nesting));
        if (sb.length() > 0) {
            sb.append(" > ");
        }
        sb.append(nodeName);
        return sb.toString();
    }

    private String printNesting(List<Node> nesting, Object nodeName) {
        if (nodeName instanceof String) {
            return printNesting(nesting, (String) nodeName);
        } else {
            return printNesting(nesting, nodeName.getClass().getName());
        }
    }


    private String printNesting(List<Node> nesting) {
        StringBuilder sb = new StringBuilder();
        for (Node n : nesting) {
            if (sb.length() > 0) {
                sb.append(" > ");
            }
            if (n.keyType == null || n.keyType.equals(String.class)) {
                sb.append(n.name);
            }
            else {
                sb.append(n.keyType.getSimpleName());
                sb.append('[');
                sb.append(n.name);
                sb.append(']');
            }
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapChecker that = (MapChecker) o;
        return Objects.equals(schema, that.schema);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schema);
    }
}
