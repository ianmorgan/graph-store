package ianmorgan.docstore

import graphql.GraphQL
import ianmorgan.docstore.dal.DocsDao
import io.javalin.ApiBuilder
import io.javalin.Javalin

/**
 * Expose operations on the graphQL schema
 */
class SchemaController constructor(dao: DocsDao, graphQL: GraphQL) {
    private val theDao = dao
    private val graphQL = graphQL

    fun register(app: Javalin) {


        app.routes {
            ApiBuilder.get("/schema") { ctx ->
                ctx.contentType("text/plain")
                ctx.result(theDao.schema())
            }
        }
    }
}