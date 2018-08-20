package ianmorgan.graphstore.graphql;


/**
 * Navigates the query args. Basically this is a map that represents
 * the underlying gradually query, and as we recurse through the graph
 * we need to keep track of which part of the tree matters. Slightly
 * similar to walking a directory tree
 *
 * @constructor Pass the current path or "/" string for root and the args under that node
 */
class ArgsWalker constructor(path : String, args : Map<String,Map<String,Any>>) {
    val path = path
    val args = args


    /**
     * The current path, i.e. the node in the graph
     */
    fun path() : String {
        return path
    }


    /**
     * The current set of fields (args) under the node
     */
    fun args() : Map<String,Map<String,Any>> {
        return args
    }

    /**
     * Walk down into the node
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

        return ArgsWalker(path, working)
    }

}
