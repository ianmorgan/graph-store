package ianmorgan.docstore

import ianmorgan.docstore.dal.DocDao
import io.javalin.ApiBuilder
import io.javalin.ApiBuilder.path
import io.javalin.Context
import io.javalin.Javalin
import org.json.JSONObject


class Controller constructor(stateHolder: StateHolder) {
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
            ApiBuilder.get("/graphql") { ctx ->

                val query = ctx.queryParam("query")

                if (query != null) {
                    val executionResult = stateHolder.graphQL().execute(query)
                    if (executionResult.errors.isEmpty()){
                        val result = mapOf("data" to executionResult.getData<Any>());
                        ctx.json(result)
                    }
                    else {
                        val result = mapOf("errors" to executionResult.errors);
                        ctx.json(result)
                    }
                }
            }

            ApiBuilder.post("/graphql") { ctx ->
                val query = extractPayload(ctx, listOf("query", "payload"))

                val executionResult = stateHolder.graphQL().execute(query)
                if (executionResult.errors.isEmpty()){
                    val result = mapOf("data" to executionResult.getData<Any>());
                    ctx.json(result)
                }
                else {
                    val result = mapOf("errors" to executionResult.errors);
                    ctx.json(result)
                }

            }


//            ApiBuilder.post("/doc/:type") {
//
//            }


            path("docs") {
                path(":type") {
                    // pure document form - all data including aggregateId in doc
                    ApiBuilder.post() { ctx ->
                        val docType = ctx.param("type")!!
                        val json = JSONObject(ctx.body())
                        val dao = stateHolder.docsDao().daoForDoc(docType)
                        if (dao is DocDao) {
                            dao.store(json.toMap())
                            ctx.result("{}")
                        }
                    }

                    // rest style - aggregateId in URL
                    path(":aggregateId") {

                        ApiBuilder.post() { ctx ->
                            val docType = ctx.param("type")!!
                            val json = JSONObject(ctx.body())
                            val dao = stateHolder.docsDao().daoForDoc(docType)

                            if (dao is DocDao){
                                // aggregateId from URL
                                val aggregateId = ctx.param("aggregateId")!!
                                json.put(dao.aggregateKey(), aggregateId)

                                dao.store(json.toMap())
                                ctx.result("{}")
                            }
                        }

                        ApiBuilder.get() { ctx ->
                            val docType = ctx.param("type")!!
                            val aggregateId = ctx.param("aggregateId")!!
                            val dao = stateHolder.docsDao().daoForDoc(docType)
                            val doc = dao.retrieve(aggregateId)
                            ctx.json(mapOf("data" to doc))
                        }

                        ApiBuilder.delete() { ctx ->
                            val docType = ctx.param("type")!!
                            val aggregateId = ctx.param("aggregateId")!!
                            val dao = stateHolder.docsDao().daoForDoc(docType)
                            if (dao is DocDao) {
                                dao.delete(aggregateId)
                            }
                        }
                    }
                }
                ApiBuilder.post() { ctx ->
                    val json = extractJson(ctx)
                    val payload = json.toMap()

                    val docType = payload["docType"] as String
                    payload.remove("docType")
                    val dao = stateHolder.docsDao().daoForDoc(docType)
                    if (dao is DocDao) {
                        dao.store(payload)
                        ctx.result("{}")
                    }
                }
            }

            path("interfaces") {
                path(":type") {

                    // rest style - aggregateId in URL
                    path(":aggregateId") {

                        ApiBuilder.get() { ctx ->
                            val interfaceType = ctx.param("type")!!
                            val aggregateId = ctx.param("aggregateId")!!
                            val dao = stateHolder.docsDao().daoForInterface(interfaceType)
                            val doc = dao.retrieve(aggregateId)
                            ctx.json(mapOf("data" to doc))
                        }
                    }
                }
            }

            ApiBuilder.get("/") { ctx ->
                ctx.redirect("/index.html")
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
