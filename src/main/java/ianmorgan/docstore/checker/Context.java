package ianmorgan.docstore.checker;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Holds context information as the validation id run.
 *
 */
public class Context {
    public List<String> failures = new ArrayList<>();
    public Stack<Node> nesting = new Stack<>();
}