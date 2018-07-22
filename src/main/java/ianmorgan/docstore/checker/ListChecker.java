package ianmorgan.docstore.checker;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Allows the specification of basic schema rules for a message or
 * data structure as a set of key values. A simple implementation of the
 * schema specification & validation approach of GraphQL (http://graphql.org/)
 */
public class ListChecker {

    private MapChecker schemaChecker;
    private boolean hasNestedMap;

    /**
     * A list in which each item is a map
     *
     * @param schema
     */
    public ListChecker(Map<Object, Object> schema) {
        this.schemaChecker = new MapChecker(schema);
        this.hasNestedMap = true;
    }

    /**
     * A list in which each item is a map
     *
     * @param schema
     */
    public ListChecker(MapChecker schema) {
        this.schemaChecker = schema;
        this.hasNestedMap = true;
    }

    /**
     * A list in which each item is single scalar value
     *
     * @param scalarType
     */
    public ListChecker(Object scalarType) {
        Map<Object, Object> schema = new HashMap<>();
        schema.put("scalar", scalarType);
        this.schemaChecker = new MapChecker(schema);
        this.hasNestedMap = false;
    }


    /**
     * Run all validations and return false if there are any problems.
     *
     * @param input
     * @return
     */
    public boolean isValid(List<Object> input) {
        return validate(input).isEmpty();
    }

    /**
     * Run all validations and return the full list of problems
     *
     * @param input
     * @return
     */
    public List<String> validate(List<Object> input) {
        Context ctx = new Context();
        validate(ctx, input);
        return ctx.failures;
    }




    public List<String> validate(Context ctx, List<Object> rawInput) {
        if (hasNestedMap) {
            List<Map<String, Object>> input = (List<Map<String, Object>>) (Object) rawInput;

            int i = 0;
            for (Object item : input) {
                ctx.nesting.add(new Node(NodeType.Type, "[" + i + "]", item));
                if (item instanceof Map) {
                    schemaChecker.validate(ctx, (Map<String, Object>) item);
                } else {
                    ctx.failures.add(printNesting(ctx.nesting) + " : " + item + " is not a map");
                }
                i++;
                ctx.nesting.pop();
            }

        } else {
            int i = 0;
            for (Object item : rawInput) {
                ctx.nesting.add(new Node(NodeType.Type, "[" + i + "]", item));

                // Make up a single item map to validate the scalar
                Map<String, Object> input = new HashMap<>();
                input.put("scalar", item);
                schemaChecker.validate(ctx, input);
                ctx.nesting.pop();
                i++;
            }
        }

        return ctx.failures;
    }

//    private String printNesting(List<Node> nesting, String nodeName) {
//        StringBuilder sb = new StringBuilder(printNesting(nesting));
//        if (sb.length() > 0) {
//            sb.append(" > ");
//        }
//        sb.append(nodeName);
//        return sb.toString();
//    }

    private String printNesting(List<Node> nesting) {
        StringBuilder sb = new StringBuilder();
        for (Node n : nesting) {
            if (sb.length() > 0) {
                sb.append(" > ");
            }
            sb.append(n.name);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListChecker that = (ListChecker) o;
        return hasNestedMap == that.hasNestedMap &&
                Objects.equals(schemaChecker, that.schemaChecker);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schemaChecker);
    }
}
