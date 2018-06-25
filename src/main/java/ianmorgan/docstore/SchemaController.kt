package ianmorgan.docstore

import graphql.GraphQL
import ianmorgan.docstore.dal.DocsDao
import io.javalin.ApiBuilder
import io.javalin.Javalin

/**
 * Expose operations on the graphQL schema
 */
class SchemaController constructor(stateHolder: StateHolder) {
    private val stateHolder = stateHolder

    fun register(app: Javalin) {

        app.routes {
            ApiBuilder.get("/schema") { ctx ->
                ctx.contentType("text/plain")
                ctx.result(stateHolder.docsDao().schema())
            }

            ApiBuilder.post("/schema") { ctx ->
                val schema = ctx.body()

                try {
                    stateHolder.build(schema)
                }
                catch (ex : RuntimeException){
                    ex.printStackTrace()
                    // todo - a fail response
                }

                // todo - fill in a proper result
                ctx.result("{}")
            }
        }
    }
}