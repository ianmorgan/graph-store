package ianmorgan.docstore

import graphql.GraphQL
import io.javalin.ApiBuilder
import io.javalin.ApiBuilder.path
import io.javalin.Javalin
import org.json.JSONObject


class Controller constructor(dao: DocsDao, graphQL: GraphQL) {
    private val theDao = dao
    private val graphQL = graphQL


    fun register(app: Javalin) {
        app.exception(Exception::class.java) { e, ctx ->
            // build the standard error response
            ctx.status(500)
            val payload = mapOf("message" to e.message,
                "stackTrace" to e.stackTrace.joinToString("\n"))
            ctx.json(mapOf("errors" to listOf(payload)))
        }

        app.routes {
            ApiBuilder.get("/graphql") { ctx ->

                val query = ctx.queryParam("query")

                if (query != null) {
                    val executionResult = graphQL.execute(query)
                    println(executionResult.getData<Any>().toString())

                    // todo - what about errors

                    val result = mapOf("data" to executionResult.getData<Any>());
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
                        val dao = theDao.daoForDoc(docType)
                        dao.store(json.toMap())
                        ctx.result("{}")
                    }

                    // rest style - aggregateId in URL
                    path(":aggregateId") {

                        ApiBuilder.post() { ctx ->
                            val docType = ctx.param("type")!!
                            val json = JSONObject(ctx.body())
                            val dao = theDao.daoForDoc(docType)

                            // aggregateId from URL
                            val aggregateId = ctx.param("aggregateId")!!
                            json.put(dao.aggregateKey(), aggregateId)

                            dao.store(json.toMap())
                            ctx.result("{}")
                        }

                        ApiBuilder.get() { ctx ->
                            val docType = ctx.param("type")!!
                            val aggregateId = ctx.param("aggregateId")!!
                            val dao = theDao.daoForDoc(docType)
                            val doc = dao.retrieve(aggregateId)
                            ctx.json(mapOf("data" to doc))
                        }

                        ApiBuilder.delete() { ctx ->
                            val docType = ctx.param("type")!!
                            val aggregateId = ctx.param("aggregateId")!!
                            val dao = theDao.daoForDoc(docType)
                            val doc = dao.delete(aggregateId)
                        }

                    }
                }
            }


        }
    }
}
