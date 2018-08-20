package ianmorgan.graphstore.graphql

import graphql.language.ObjectTypeDefinition
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import graphql.schema.idl.TypeRuntimeWiring
import ianmorgan.graphstore.dal.DocsDao
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

        println (args.keys)

        val defs = env.selectionSet.definitions

        println (defs.keys)


        if (data != null) {
            val helper = Helper.build(typeDefinition)
            for (f in helper.listTypeFieldNames()) {

                val typeName = helper.typeForField(f)

                // is this an embedded doc
                fetchEmbeddedDoc(typeName, data, f,argsHelper, args)

                // is this an embedded interface
                fetchEmbeddedInterface(typeName, data, f, argsHelper, args)

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

    private fun fetchEmbeddedDoc(
        typeName: String?,
        data: HashMap<String, Any>,
        field: String,
        fieldSetHelper : DataFetchingFieldSelectionSetHelper,
        args : Map<String,Map<String,Any>>
    ) {
        if (dao.availableDocs().contains(typeName)) {
            var ids = data.getOrDefault(field, emptyList<String>()) as List<String>
            data.put("$" + field + "Raw" ,ids)  // preserve the raw values

            val filtered  = applyPaginationFilters(fieldSetHelper, field, ids)

            val expanded = ArrayList<Map<String, Any>>()
            for (theId in filtered) {
                val x = lookupDocById(typeName!!, theId)
                if (x != null) {
                    expanded.add(x)
                }
            }
            data.put(field, expanded)
        }
    }

    private fun fetchEmbeddedInterface(
        typeName: String?,
        data: HashMap<String, Any>,
        field: String,
        fieldSetHelper : DataFetchingFieldSelectionSetHelper,
        args : Map<String,Map<String,Any>>

    ) {
        if (dao.availableInterfaces().contains(typeName)) {
            val ids = data.getOrDefault(field, emptyList<String>()) as List<String>
            data.put("$" + field + "Raw" ,ids)  // preserve the raw values

            // process argument to the collections
            val filtered = applyPaginationFilters(fieldSetHelper, field, ids)

            val expanded = ArrayList<Map<String, Any>>()
            for (theId in filtered) {
                val ex = lookupInterfaceById(typeName!!, theId)
                if (ex != null) {
                    expanded.add(ex)
                }
            }
            data.put(field, expanded)

        }
    }

    private fun applyPaginationFilters(
        fieldSetHelper: DataFetchingFieldSelectionSetHelper,
        field: String,
        ids: List<String>
    ): List<String> {
        var result = ids
        val args = fieldSetHelper.argsForField(field)
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
