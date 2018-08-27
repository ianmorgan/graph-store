package ianmorgan.graphstore.graphql

import graphql.language.ObjectTypeDefinition
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import graphql.schema.idl.TypeDefinitionRegistry
import ianmorgan.graphstore.dal.DocsDao
import java.util.HashMap
import kotlin.collections.ArrayList
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.contains
import kotlin.collections.emptyList
import kotlin.collections.isNotEmpty
import kotlin.collections.mapOf
import kotlin.collections.set
import kotlin.collections.take

/**
 * A DataFetcher for a single doc, linked to its DAO. This fetcher is passed the
 * complete ObjectTypeDefinition and also knows how to resolve data for child nodes, which requires
 * recursive calls to the fetchers.
 */
@Suppress("UNCHECKED_CAST")
class DocDataFetcher constructor(
    docsDao: DocsDao,
    typeDefinition: ObjectTypeDefinition,
    registry: TypeDefinitionRegistry
) :
    DataFetcher<Map<String, Any>?> {
    val dao = docsDao
    val docType = typeDefinition.name
    val typeDefinition = typeDefinition
    val registry = registry

    /**
     * Entry point when called by GraphQLJava API.
     */
    override fun get(env: DataFetchingEnvironment): Map<String, Any>? {

        // lookup ID
        val idFieldName = dao.daoForDoc(docType).aggregateKey()
        val id = env.getArgument<String>(idFieldName)

        // find by ID
        val data = lookupDocById(docType, id)

        if (data != null) {
            val walker = ArgsWalker(env.selectionSet.arguments)
            val helper = Helper.build(typeDefinition)

            // Recurse and find embedded lists
            for (field in helper.listTypeFieldNames()) {

                val typeName = helper.typeForField(field)

                if (walker.hasChild(field)) {

                    val child = walker.walkPath(field)

                    // try as an embedded doc
                    fetchEmbeddedDoc(typeName, data, field, child)

                    // try as an embedded interface
                    fetchEmbeddedInterface2(data, child)
                }
            }

            applyCountPseudoField(data)
        }
        return data
    }

    private fun applyCountPseudoField(data: HashMap<String, Any>) {
        // note that the order in which steps are run is important here
        for (f in typeDefinition.fieldDefinitions) {
            if (f.name.endsWith("Count")) {
                val rawField = "$" + f.name.replace("Count", "") + "Raw"
                val ids = data.getOrDefault(rawField, emptyList<String>()) as List<String>
                data[f.name] = ids.size
            }
        }
    }

    private fun applyCountPseudoField(data: HashMap<String, Any>, rawIds:List<String>) {
        // note that the order in which steps are run is important here
        for (f in typeDefinition.fieldDefinitions) {
            if (f.name.endsWith("Count")) {
//                val rawField = "$" + f.name.replace("Count", "") + "Raw"
//                val ids = data.getOrDefault(rawField, emptyList<String>()) as List<String>
                data[f.name] = rawIds.size
            }
        }
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
        walker: ArgsWalker
    ) {
        if (dao.availableDocs().contains(docType)) {
            var ids = data.getOrDefault(field, emptyList<String>()) as List<String>
            data.put("$" + field + "Raw", ids)  // preserve the raw values
            data.put("#docType", docType!!)

            val filtered = applyPaginationFilters(field, ids, walker)


            val expanded = ArrayList<Map<String, Any>>()
            for (theId in filtered) {
                val x = lookupDocById(docType!!, theId)
                if (x != null) {
                    if (walker.children().isNotEmpty()) {
                        println(" ** LOOK FOR SOME CHILDREN ** ")
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
     * @param data    A HashMap with the data. This will be updated with the retrieved doc(s)
     * @param walker  The {@link ArgsWalker}, used to recursively extract the fields & args passed to the query. The
     *                node() element tells us where we are in the query tree
     *                (see https://ianmorgan.github.io/graph-store/queryProcessingSteps)
     *
     */
    private fun fetchEmbeddedInterface2(data: HashMap<String, Any>, walker: ArgsWalker) {
        val field = walker.node()
        val helper = Helper.build(typeDefinition)
        val docType = helper.typeForField(field)

        // do nothing - this isn't an interface
        if (!dao.availableInterfaces().contains(docType)) {
            return
        }

        val ids = data[field] as List<String>?
        if (ids != null) {
            data.put("$" + field + "Raw", ids)  // preserve the raw values
            val filtered = applyPaginationFilters(field, ids, walker)

            val helper = Helper.build(typeDefinition)
            val docType = helper.typeForField(field)
            val interfaceDataFetcher = InterfaceDataFetcher(dao, docType!!, registry)

            val expanded = ArrayList<Map<String, Any>>()

            for (theId in filtered) {
                val ex = interfaceDataFetcher.get(mapOf("id" to theId))
                if (ex != null) {
                    val working = HashMap(ex)

                    if (walker.children().isNotEmpty()) {
                        for (child in walker.children()) {
                            fetchEmbeddedInterface2(working, child)
                        }
                    }

                    expanded.add(working)
                }
            }

            data[field] = expanded
            applyCountPseudoField(data)

        }

    }


    private fun applyPaginationFilters(
        field: String,
        ids: List<String>,
        walker: ArgsWalker
    ): List<String> {
        var result = ids
        //val args = fieldSetHelper.argsForField(field)
        val args = walker.args()["/"]
        if (args != null) {
            if (args.containsKey("first")) {
                val first = args.get("first") as Int
                if (first < result.size) {
                    result = result.subList(first, result.size)
                } else {
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


    private fun lookupDocById(docType: String, id: String): HashMap<String, Any>? {
        val data = dao.daoForDoc(docType).retrieve(id)
        if (data != null) {
            val result = HashMap(data)
            result["#docType"] = docType
            return result
        } else {
            return null;
        }
    }

}
