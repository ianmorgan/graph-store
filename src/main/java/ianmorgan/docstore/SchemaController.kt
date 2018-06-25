package ianmorgan.docstore


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
                val schema = extractPayload(ctx)
                val result = HashMap<Any,Any>()

                if (!stateHolder.build(schema)){

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