package ianmorgan.docstore

import graphql.GraphQL
import ianmorgan.docstore.dal.DocsDao
import io.javalin.ApiBuilder
import io.javalin.Context
import io.javalin.Javalin
import org.json.JSONObject

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
                val schema = extractPayload(ctx)

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

    private fun extractPayload(ctx: Context): String {
        if (ctx.formParamMap().containsKey("payload")){
            return ctx.formParam("payload")!!
        }
        if (ctx.formParamMap().containsKey("schema")){
            return ctx.formParam("schema")!!
        }
        return ctx.body()
    }
}