package ianmorgan.graphstore

import graphql.GraphQL
import ianmorgan.graphstore.dal.DocsDao
import ianmorgan.graphstore.dal.EventStoreClient
import ianmorgan.graphstore.dal.ReaderDao
import ianmorgan.graphstore.graphql.GraphQLFactory
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

    fun rebuild(graphQLSchema: String) : Boolean {
        return build(graphQLSchema,docsDao.externalDaos())
    }

    /**
     * Rebuild using the provided schema
     */
    fun build(graphQLSchema: String, externalDaos: Map<String, ReaderDao>) : Boolean {
        try {
            val dao = DocsDao(graphQLSchema, eventStoreClient, externalDaos)
            docsDao = dao

            val ql = GraphQLFactory.build(graphQLSchema, dao)
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

    fun build(schemaFile: File, externalDaos: HashMap<String, ReaderDao>) : Boolean {
        val schemaString = FileInputStream(schemaFile).bufferedReader().use { it.readText() }
        return build(schemaString, externalDaos)
    }

    fun docsDao(): DocsDao {
        if (!isValid()) throw RuntimeException("DocsDao unavailable")
        return docsDao
    }

    fun graphQL(): GraphQL {
        if (!isValid()) throw RuntimeException("GraphQL unavailable")
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

    fun eventStore () : EventStoreClient {
        return eventStoreClient
    }



}