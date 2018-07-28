package ianmorgan.graphstore


import io.javalin.ApiBuilder
import io.javalin.Context
import io.javalin.Javalin
import org.json.JSONObject


class AdminController constructor(stateHolder: StateHolder) {
    private val stateHolder = stateHolder

    fun register(app: Javalin) {
        app.exception(Exception::class.java) { e, ctx ->
            // build the standard error response
            ctx.status(500)
            val payload = mapOf(
                "message" to e.message,
                "stackTrace" to e.stackTrace.joinToString("\n")
            )
            ctx.json(mapOf("errors" to listOf(payload)))
        }


        app.routes {


            ApiBuilder.path("admin") {
                ApiBuilder.path("events") {
                    ApiBuilder.path("configuration") {
                        ApiBuilder.get() { ctx ->
                            val events = stateHolder
                                .eventStore()
                                .eventsForType(setOf("ExternalDaoRegistered", "ExternalDaoConfigured"))
                            ctx.json(mapOf("data" to events))
                        }
                    }

                    ApiBuilder.path(":aggregateId") {
                        ApiBuilder.get() { ctx ->
                            val aggregateId = ctx.param("aggregateId")!!
                            val events = stateHolder
                                .eventStore()
                                .events(aggregateId)
                            ctx.json(mapOf("data" to events))
                        }
                    }

                }

                ApiBuilder.path("docs") {
                    ApiBuilder.get() { ctx ->
                        val result = mutableListOf<Map<String,String>>()

                        for (doc in stateHolder.docsDao().availableDocs()){
                            val dao = stateHolder.docsDao().daoForDoc(doc)
                            result.add (mapOf ("name" to doc,
                                "implemetation" to dao::class.java.canonicalName!!,
                                "type" to "doc"))
                        }

                        for (doc in stateHolder.docsDao().availableInterfaces()){
                            val dao = stateHolder.docsDao().daoForInterface(doc)
                            result.add (mapOf ("name" to doc,
                                "implemetation" to dao::class.java.canonicalName!!,
                                "type" to "interface"))
                        }

                        ctx.json(mapOf("data" to result))
                    }
                }

            }

            ApiBuilder.get("/") { ctx ->
                ctx.redirect("/admin/index.html")
            }

        }


    }

    private fun extractJson(ctx: Context): JSONObject {
        if (ctx.formParamMap().containsKey("payload")) {
            return JSONObject(ctx.formParam("payload"))
        }
        return JSONObject(ctx.body())

    }

    private fun extractPayload(ctx: Context, formFields: List<String> = listOf("payload")): String {
        for (field in formFields) {
            if (ctx.formParamMap().containsKey(field)) {
                return ctx.formParam(field)!!
            }
        }
        return ctx.body()
    }
}
