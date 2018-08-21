package ianmorgan.graphstore.graphql;


/**
 * Navigates the query args. Basically this is a map that represents
 * the underlying GraphQL query, and as we recurse through the graph
 * we need to keep track of which part of the tree matters. Slightly
 * similar to walking a directory tree.
 *
 * The order of the original map is retained through use of a LinkedHashMap
 *
 * @constructor Pass the current path or "/" string for root and the args under that node
 */
class ArgsWalker constructor(path : String, args : Map<String,Map<String,Any>>, parent : ArgsWalker? = null) {
    val path = if (path.endsWith("/")) path else "$path/"
    val args = args
    val parent = parent


    /**
     * The current path, i.e. the node in the graph. The trailing slash is
     * removed unless this the root, e.g. root will be "/", whereas the a child
     * node would be "friends"
     */
    fun path() : String {
        return if (path == "/") path else path.substring(0,path.length-1)
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
     * Walk down into the node
     *
     * @param path - The path, can be with or without the trailing slash (same basic
     *               rules as directory listing in bash), e.g. "friends" or "friends/"
     */
    fun walkPath(path : String) : ArgsWalker {
        val standardPath = if (path.endsWith("/")) path else "$path/"

        val working = LinkedHashMap<String,Map<String,Any>>()
        for (key in args.keys){
            if (key.startsWith(standardPath) ){
                val trimmed = key.substring(standardPath.length)
                working.put(trimmed,args.get(key)!!);
            }
        }

        return ArgsWalker(standardPath, working, this)
    }

}
