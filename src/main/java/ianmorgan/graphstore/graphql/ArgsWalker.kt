package ianmorgan.graphstore.graphql;


/**
 * Navigates the query args. Basically this is a map that represents
 * the underlying GraphQL query, and as we recurse through the graph
 * we need to keep track of which part of the tree matters. Slightly
 * similar to walking a directory tree.
 *
 * The order of the original map is retained through use of a LinkedHashMap
 *
 * @constructor Pass the current node or "/" string for root and the args under that node
 */
class ArgsWalker constructor(node : String, args : Map<String,Map<String,Any>>, parent : ArgsWalker?) {
    val node = if (node.endsWith("/")) node else "$node/"
    val args = args
    val parent = parent

    /**
     * Build the root node. This is always named "root"
     */
    constructor(args : Map<String,Map<String,Any>>) : this("root/", args, null)


    /**
     * The current node, i.e. the node in the graph. The trailing slash is
     * removed unless this the root, e.g. root will be "/", whereas the a child
     * node would be "friends"
     */
    fun node() : String {
        return if (node == "/") node else node.substring(0,node.length-1)
    }

    /**
     * The full path back to the root
     */
    fun fullPath() : String {
        var node : ArgsWalker? = this
        var working = node()
        while (node?.parent() !=null){
            node = node.parent()
            if (!node?.isRoot()!!) {
                working = node?.node() + "/" + working
            }
        }
        return working
    }

    /**
     * The current set of fields (args) under the node
     */
    fun args() : Map<String,Map<String,Any>> {
        return args
    }

    /**
     * The node above, or null if this is the top of the tree
     */
    fun parent() : ArgsWalker? {
        return parent
    }

    /**
     * Walk down into the (child) node.
     *
     * @param path - The node, can be with or without the trailing slash (same basic
     *               rules as directory listing in bash), e.g. "friends" or "friends/"
     */
    fun walkPath(path : String) : ArgsWalker {
        val standardPath = if (path.endsWith("/")) path else "$path/"
        val trimmedPath = standardPath.substring(0,standardPath.length-1)

        val working = LinkedHashMap<String,Map<String,Any>>()
        for (key in args.keys){
            if (key.startsWith(standardPath) ){
                val trimmed = key.substring(standardPath.length)
                working.put(trimmed,args.get(key)!!);
            }
        }

        // Add the "/" root node
        val root = args[trimmedPath]
        if (root != null && root.isNotEmpty()){
            working["/"] = root
        }

        return ArgsWalker(standardPath, working, this)
    }


    /**
     *
     */
    fun children() : List<ArgsWalker> {
        val working = ArrayList<ArgsWalker>()
        val nodes = HashSet<String>()
        for (key in args.keys){
            if (key.contains("/") ){
                val path = key.split("/")[0]
                if (!nodes.contains(path)) {
                    working.add(this.walkPath(path));
                    nodes.add(path)
                }
            }
        }
        return working;
    }

    fun hasChild(path : String): Boolean {
        for (child in children()){
            if (child.node() == path){
                return true;
            }
        }
        return false
    }

    fun isRoot() : Boolean {
        return node == "root/"
    }


}
