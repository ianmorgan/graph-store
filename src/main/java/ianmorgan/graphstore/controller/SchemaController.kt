package ianmorgan.graphstore.controller


import ianmorgan.graphstore.StateHolder
import io.javalin.ApiBuilder
import io.javalin.Context
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
                val schema = Helper.build(ctx).extractPayload()
                val result = HashMap<Any,Any>()

                if (!stateHolder.rebuild(schema)){

                    val error =
                        mapOf("message" to "Problem parsing the GraphQL schema",
                              "schemaError" to stateHolder.exception()!!.message)
                        result["errors"] = listOf(error)
                    ctx.status(500)
                }

                ctx.json(result)
            }
        }
    }

}