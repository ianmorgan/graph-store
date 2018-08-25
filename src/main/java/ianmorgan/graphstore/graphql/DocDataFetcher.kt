package ianmorgan.graphstore.graphql

import graphql.language.ObjectTypeDefinition
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import graphql.schema.idl.TypeRuntimeWiring
import ianmorgan.graphstore.dal.DocsDao
import org.json.JSONObject
import java.util.HashMap

/**
 * A DataFetcher for a single doc, linked to its DAO. This fetcher is passed the
 * complete ObjectTypeDefinition and also knows how to resolve data for child nodes, which requires
 * recursive calls to the DAOs.
 */
@Suppress("UNCHECKED_CAST")
class DocDataFetcher constructor(docsDao: DocsDao, typeDefinition: ObjectTypeDefinition, builder : TypeRuntimeWiring.Builder) :
    DataFetcher<Map<String, Any>?> {
    val dao = docsDao
    val docType = typeDefinition.name
    val typeDefinition = typeDefinition
    override fun get(env: DataFetchingEnvironment): Map<String, Any>? {

        val idFieldName = dao.daoForDoc(docType).aggregateKey()
        val id = env.getArgument<String>(idFieldName)
        val data = lookupDocById(docType,id)

        val argsHelper = Helper.build(env.selectionSet)

        val args = env.selectionSet.arguments
        val walker = ArgsWalker("/",args)

        println (args.keys)

        println (JSONObject(args).toString(2))

        val defs = env.selectionSet.definitions

        println (defs.keys)


        if (data != null) {
            val helper = Helper.build(typeDefinition)
            for (f in helper.listTypeFieldNames()) {

                val typeName = helper.typeForField(f)

                if (walker.hasChild(f)) {

                    val child = walker.walkPath(f)

                    // is this an embedded doc
                    fetchEmbeddedDoc(typeName, data, f, child)

                    // is this an embedded interface
                    fetchEmbeddedInterface(typeName, data, f, child)
                }

            }

            // todo - generalise this a little more as a way of dealing with "pseudo" fields
            // note that the order in which steps are run is important here
            for (f in typeDefinition.fieldDefinitions){
                if (f.name.endsWith("Count")){
                    val rawField = "$" + f.name.replace("Count","") + "Raw"
                    val ids = data.getOrDefault(rawField, emptyList<String>()) as List<String>
                    data[f.name] = ids.size
                }
            }
        }
        return data
    }

    /**
     * Fetch embedded docs, i.e. given a set of one or more ids expand those by fetching
     * the actual data via its DAO.
     *
     * As an example using the starwars_ex schema, the query
     * "{droid(id: "2001"){name,starships{name,manufacturer}}}"
     *
     * will initially return an array of ids for starships and this would
     * replace them with actual data.
     *
     * @param docType The docType, as used by the DAO layer. This is also the same as the 'type' name
     *                in the GraphQL schema, e.g. 'Droid'
     * @param data    A HashMap with the data. This will be updated with the retrieved doc(s)
     * @param field   The field to be replaced with the actual data
     * @param walker  The ArgsWalker, used to recursively extract the fields & args passed to the query.
     *
     */
    private fun fetchEmbeddedDoc(
        docType: String?,
        data: HashMap<String, Any>,
        field: String,
        walker : ArgsWalker
    ) {
        if (dao.availableDocs().contains(docType)) {
            var ids = data.getOrDefault(field, emptyList<String>()) as List<String>
            data.put("$" + field + "Raw" ,ids)  // preserve the raw values

            val filtered  = applyPaginationFilters(field, ids, walker)



            val expanded = ArrayList<Map<String, Any>>()
            for (theId in filtered) {
                val x = lookupDocById(docType!!, theId)
                if (x != null) {
                    if (walker.children().isNotEmpty()){
                        println (" ** LOOK FOR SOME CHILDREN ** ")
                        for (child in walker.children()) {
                            fetchEmbeddedInterface2(x, child)
                        }
                    }
                    expanded.add(x)
                }
            }
            data.put(field, expanded)
        }
    }

    private fun fetchEmbeddedInterface2(data: HashMap<String, Any>, walker: ArgsWalker) {
        val field = walker.path()
        val ids = data[field] as List<String>?
        if (ids != null){
            println ("fetching ids " + ids)
            data.put("$" + field + "Raw" ,ids)  // preserve the raw values

            val filtered = applyPaginationFilters(field, ids, walker)


            val helper = Helper.build(typeDefinition)
            val docType = helper.typeForField(field)


            val expanded = ArrayList<Map<String, Any>>()
            for (theId in filtered) {
                val x = lookupInterfaceById(docType!!, theId)
                if (x != null) {
//                    if (walker.children().isNotEmpty()){
//                        println (" ** LOOK FOR SOME CHILDREN ** ")
//                        for (child in walker.children()) {
//                            fetchEmbeddedInterface2(x, child)
//                        }
//                    }
                    expanded.add(x)
                }
            }
            data[field] = expanded

        }

    }

    /**
     * Fetch embedded interface, i.e. given a set of one or more ids expand those by fetching
     * the actual data via its DAO.
     *
     * As an example using the starwars_ex schema, the query
     * "{droid(id: "2001"){name,friends{name}}}"
     *
     * will initially return an array of ids for friends and this would
     * replace them with actual data.
     *
     * @param docType The docType, as used by the DAO layer. This is also the same as the 'type' name
     *                in the GraphQL schema, e.g. 'Droid'
     * @param data    A HashMap with the data. This will be updated with the retrieved doc(s)
     * @param field   The field to be replaced with the actual data
     * @param walker  The ArgsWalker, used to recursively extract the fields & args passed to the query.
     *
     */
    private fun fetchEmbeddedInterface(
        typeName: String?,
        data: HashMap<String, Any>,
        field: String,
        walker : ArgsWalker
    ) {
        if (dao.availableInterfaces().contains(typeName)) {
            val ids = data.getOrDefault(field, emptyList<String>()) as List<String>
            data.put("$" + field + "Raw" ,ids)  // preserve the raw values

            val filtered = applyPaginationFilters(field, ids, walker)

            val expanded = ArrayList<Map<String, Any>>()
            for (theId in filtered) {
                val ex = lookupInterfaceById(typeName!!, theId)

                if (walker.children().isNotEmpty()){
                    println (" ** LOOK FOR SOME CHILDREN ** ")

                }
                if (ex != null) {
                    for (child in walker.children()) {
                        fetchEmbeddedInterface2(ex, child)
                    }
                    expanded.add(ex)
                }
            }
            data.put(field, expanded)

            val children = walker.children()
            val helper = Helper.build(typeDefinition)
//            for (child in children){
//
//                val f = child.path()// todo - need to return the end of the path
//
//
//                val typeName = helper.typeForField(f)
//
//                // is this an embedded doc
//                //fetchEmbeddedDoc(typeName, data, f, child)
//
//                // is this an embedded interface
//                fetchEmbeddedInterface(typeName, data, f, child)
//            }

        }
    }

    private fun applyPaginationFilters(
        field: String,
        ids: List<String>,
        walker : ArgsWalker
    ): List<String> {
        var result = ids
        //val args = fieldSetHelper.argsForField(field)
        val args = walker.args()["/"]
        if (args != null) {
            if (args.containsKey("first")) {
                val first = args.get("first") as Int
                if (first < result.size) {
                    result = result.subList(first, result.size)
                }
                else {
                    result = ArrayList()
                }
            }

            if (args.containsKey("count")) {
                val count = args.get("count") as Int
                if (count < result.size) {
                    result = result.take(count)
                }
            }
        }
        return result
    }


    private fun lookupDocById(docType : String, id: String): HashMap<String, Any>? {
        val data = dao.daoForDoc(docType).retrieve(id)
        if (data != null) {
            val result = HashMap(data)
            result["#docType"] = docType
            return result
        } else {
            return null;
        }
    }

    private fun lookupInterfaceById(interfaceName : String, id: String): HashMap<String, Any>? {
        val data = dao.daoForInterface(interfaceName).retrieve(id)
        if (data != null) {
            return HashMap(data);
        } else {
            return null;
        }
    }
}
