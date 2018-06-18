package ianmorgan.docstore.graphql

import graphql.language.ObjectTypeDefinition
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import graphql.schema.idl.TypeRuntimeWiring
import ianmorgan.docstore.dal.DocsDao
import ianmorgan.docstore.dal.InterfaceDao
import java.util.HashMap
import kotlin.collections.ArrayList
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.emptyList
import kotlin.collections.emptyMap

/**
 * A DataFetcher for a single doc, linked to its DAO. This fetcher is passed the
 * complete ObjectTypeDefinition and also knows how to resolve data for child nodes, which requires
 * recursive calls to the DAOs.
 */
class DocDataFetcher constructor(docsDao: DocsDao, typeDefinition: ObjectTypeDefinition, builder : TypeRuntimeWiring.Builder) :
    DataFetcher<Map<String, Any>?> {
    val dao = docsDao
    val docName = typeDefinition.name
    val typeDefinition = typeDefinition
    override fun get(env: DataFetchingEnvironment): Map<String, Any>? {

        val idFieldName = dao.daoForDoc(docName).aggregateKey()
        val id = env.getArgument<String>(idFieldName)
        val data = lookupDocById(docName,id)

        if (data != null) {
            val helper = Helper.build(typeDefinition)
            for (f in helper.listTypeFieldNames()) {
                val typeName = helper.typeForField(f)

                // is this an embedded doc
                fetchEmbeddedDoc(typeName, data, f)

                // is this an embedded interface
                fetchEmebbedInterface(typeName, data, f)
            }
        }
        return data
    }

    private fun fetchEmbeddedDoc(
        typeName: String?,
        data: HashMap<String, Any>,
        f: String
    ) {
        if (dao.availableDocs().contains(typeName)) {
            val ids = data.getOrDefault(f, emptyList<String>()) as List<String>
            val expanded = ArrayList<Map<String, Any>>()
            for (theId in ids) {
                val x = lookupDocById(typeName!!, theId)
                if (x != null) {
                    expanded.add(x)
                }
            }
            data.put(f, expanded)

        }
    }

    private fun fetchEmebbedInterface(
        typeName: String?,
        data: HashMap<String, Any>,
        f: String
    ) {
        if (dao.availableInterfaces().contains(typeName)) {
            val ids = data.getOrDefault(f, emptyList<String>()) as List<String>
            val expanded = ArrayList<Map<String, Any>>()
            for (theId in ids) {
                val x = lookupInterfaceById(typeName!!, theId)
                if (x != null) {
                    expanded.add(x)
                }
            }
            data.put(f, expanded)

        }
    }

    private fun lookupDocById(docName : String, id: String): HashMap<String, Any>? {
        val data = dao.daoForDoc(docName).retrieve(id)
        if (data != null) {
            return HashMap(data);
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

/**
 * A DataFetcher for a single doc, linked to its DAO. This fetcher is passed the
 * complete ObjectTypeDefinition and also knows how to resolve data for child nodes, which requires
 * recursive calls to the DAOs.
 */
class DocListDataFetcher constructor(docsDao: DocsDao, typeDefinition: ObjectTypeDefinition) :
    DataFetcher<List<Map<String, Any>?>> {
    val dao = docsDao
    val docName = typeDefinition.name
    val typeDefinition = typeDefinition
    override fun get(env: DataFetchingEnvironment): List<Map<String, Any>?> {

        if (env.containsArgument("name")) {
            val name = env.getArgument<String>("name")
            return dao.daoForDoc(docName).findByField("name", name);
        }

        return emptyList()
    }
}


/**
 * A DataFetcher that just tries all docs. The most basic way of dealing with interfaces
 */
class DocsDataFetcher constructor(docsDao: DocsDao) : DataFetcher<Map<String, Any>?> {
    val daos = docsDao
    override fun get(env: DataFetchingEnvironment): Map<String, Any>? {
        val id = env.getArgument<String>("id")

        for (doc in daos.availableDocs()) {
            val data = daos.daoForDoc(doc).retrieve(id)
            if (data != null) {
                return data
            }
        }
        return null;
    }
}


/**
 * Does nothing - useful for experimenting and debugging only
 */
class NullDataFetcher : DataFetcher<Map<String, Any>?> {
    override fun get(environment: DataFetchingEnvironment?): Map<String, Any>? {
        println("In NullDataFetcher ")
        return emptyMap()
    }
}

/**
 * Return fixed data - mainly for experimenting and debugging
 */
class FixedDataFetcher constructor(data : Map<String, Any>?) : DataFetcher<Map<String, Any>?> {
    val data = data
    override fun get(environment: DataFetchingEnvironment?): Map<String, Any>? {
        println("In FixedDataFetcher ")
        return data
    }
}

/**
 * Return fixed data - mainly for experimenting and debugging
 */
class FixedListDataFetcher constructor(data : List<Map<String, Any>?>) : DataFetcher<List<Map<String, Any>?>> {
    val data = data
    override fun get(environment: DataFetchingEnvironment?): List<Map<String, Any>?> {
        println("In FixedListDataFetcher ")
        return data
    }
}

class FriendsDataFetcher constructor(dao : InterfaceDao) :  DataFetcher<List<Map<String, Any>?>> {
    val dao = dao
    override fun get(environment: DataFetchingEnvironment): List<Map<String, Any>?> {
        println("In FriendsDataFetcher ")

        val result = ArrayList<Map<String, Any>?>()

        val source = environment.getSource<Map<String,Any?>>()

        if (source.containsKey("friends")){
            for (friendId in source["friends"] as List<String>){
                val friend = dao.retrieve(friendId)
                if (friend != null){
                    result.add(friend)
                }
                else {
                    // todo - this should be adding a warning to the query
                    println ("couldnt find friend $friendId")
                }
            }
        }
       return result
    }
}

object Fetcher {

    /**
     * Entry point to fetch for a single doc. Will internally drill down through the query structure until calling
     * other fetchers as necessary, until leaf nodes with scalar values are reached
     */
    fun docFetcher(
        docsDao: DocsDao,
        typeDefinition: ObjectTypeDefinition,
        builder : TypeRuntimeWiring.Builder
    ): DataFetcher<Map<String, Any>?> {
        return DocDataFetcher(docsDao, typeDefinition, builder)
    }

    /**
     * Entry point to fetch for an interface, picking the correct document by its id. Will internally drill down
     * through the query structure until calling other fetchers as necessary, until leaf nodes with scalar values
     * are reached
     */
    fun interfaceFetcher(docsDao: DocsDao,
                         typeDefinition: ObjectTypeDefinition?): DataFetcher<Map<String, Any>?> {
        return DocsDataFetcher(docsDao)
    }

    fun docListFetcher(
        docsDao: DocsDao,
        typeDefinition: ObjectTypeDefinition
    ): DataFetcher<List<Map<String, Any>?>> {
        return DocListDataFetcher(docsDao, typeDefinition)
    }

    fun nullDocFetcher(): DataFetcher<Map<String, Any>?> {
        return NullDataFetcher()
    }
}