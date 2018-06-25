package ianmorgan.docstore

import graphql.GraphQL
import ianmorgan.docstore.dal.DocsDao
import ianmorgan.docstore.dal.EventStoreClient
import ianmorgan.docstore.graphql.GraphQLFactory2
import java.io.File
import java.io.FileInputStream

/**
 * Singleton to pass around that has all application state.
 */
class StateHolder (eventStoreClient: EventStoreClient) {
    val eventStoreClient = eventStoreClient

    lateinit var docsDao: DocsDao
    lateinit var graphQL: GraphQL
    var valid = false
    var exception: RuntimeException? = null

    /**
     * Rebuild using the provided schema
     */
    fun build(graphQLSchema: String) : Boolean {
        try {
            val dao = DocsDao(graphQLSchema, eventStoreClient)
            docsDao = dao

            val ql = GraphQLFactory2.build(graphQLSchema, dao)
            graphQL = ql

            valid = true
            exception = null
            return true
        }
        catch (ex :RuntimeException){
            exception = ex
            valid = false
            return false
        }
    }

    fun build(schemaFile: File) : Boolean {
        val schemaString = FileInputStream(schemaFile).bufferedReader().use { it.readText() }
        return build(schemaString)
    }

    fun docsDao(): DocsDao {
        if (!isValid()) throw RuntimeException("DocsDao unavailable")
        return docsDao
    }

    fun graphQL(): GraphQL {
        if (!isValid()) throw RuntimeException("DocsDao unavailable")
        return graphQL
    }

    /**
     * Is the stateholder valid, i.e. has everything been initialised ok
     */
    fun isValid () : Boolean{
        return valid
    }


    /**
     * If there has been a problem, expose it to the outside world
     */
    fun exception () : RuntimeException? {
        return exception
    }


}