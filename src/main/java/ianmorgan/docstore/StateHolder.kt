package ianmorgan.docstore

import graphql.GraphQL
import ianmorgan.docstore.dal.DocsDao
import ianmorgan.docstore.dal.EventStoreClient
import ianmorgan.docstore.graphql.GraphQLFactory2

/**
 * Singleton to pass around that has all application state.
 */
class StateHolder (eventStoreClient: EventStoreClient) {
    val eventStoreClient = eventStoreClient

    lateinit var docsDao: DocsDao
    lateinit var graphQL: GraphQL

    /**
     * Rebuild using the provided schema
     */
    fun build(graphQLSchema: String) {
        val dao = DocsDao(graphQLSchema, eventStoreClient)
        docsDao = dao

        val ql = GraphQLFactory2.build(graphQLSchema, dao)
        graphQL = ql
    }

    fun docsDao(): DocsDao {
        return docsDao
    }

    fun graphQL(): GraphQL {
        return graphQL
    }


}