package ianmorgan.docstore.checker;

public class Node {
    NodeType type;      // the type of the node,
    String name;        // the name (as it would be in JSON)
    Object schemaKey;   // the matching key found in the schmea
    Object value;       // the actual value found
    Class keyType;

    public Node(NodeType type, Object schemaKey, Object value) {
        this.type = type;
        this.schemaKey = schemaKey;
        this.name = schemaKey instanceof String ? (String) schemaKey : schemaKey.getClass().getName();
        this.value = value;
        this.keyType = schemaKey.getClass();
    }

    public Node(NodeType type, Class expectedKey, Object schemaKey, Object value) {
        this.type = type;
        this.schemaKey = schemaKey;
        this.name = schemaKey instanceof String ? (String) schemaKey : schemaKey.getClass().getName();
        this.value = value;
        this.keyType = expectedKey;
    }

    public Node(NodeType type, String name, Object value) {
        this.type = type;
        this.name = name;
        this.schemaKey = name;
        this.value = value;
        this.keyType = String.class;
    }
}