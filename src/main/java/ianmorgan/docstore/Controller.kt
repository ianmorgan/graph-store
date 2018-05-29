package ianmorgan.docstore

import graphql.GraphQL
import io.javalin.ApiBuilder
import io.javalin.Javalin
import io.javalin.ApiBuilder.path
import org.json.JSONObject


class Controller constructor(dao : DocsDao, graphQL : GraphQL){
    private val theDao = dao
    private val graphQL = graphQL


    fun register(app: Javalin) {
        app.routes {
            ApiBuilder.get("/graphql") { ctx ->

                val query = ctx.queryParam("query")

                if (query != null){
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
                //get(???({ UserController.getAllUsers() }))
                //post(???({ UserController.createUser() }))
                path(":type") {
                    ApiBuilder.post()  {ctx ->
                        val json = JSONObject(ctx.body())

                        println(ctx.param("type"))

                        println ("saving the doc")
                        println (ctx.body())

                        val dao = theDao.daoForDoc(ctx.param("type")!!)
                        dao.store(json.toMap())

                    }
                    //patch(???({ UserController.updateUser() }))
                    //delete(???({ UserController.deleteUser() }))
                }
            }


        }
    }
}
