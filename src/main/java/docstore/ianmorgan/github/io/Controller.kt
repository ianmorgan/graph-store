package docstore.ianmorgan.github.io

import graphql.GraphQL
import io.javalin.ApiBuilder
import io.javalin.Javalin

class Controller constructor(dao : DocDao, graphQL : GraphQL){
    private val theDao = dao


    fun register(app: Javalin) {
        app.routes {
            ApiBuilder.get("/graphql") { ctx ->

                //                // run the query
//                val filter = Filter.ModelMapper.fromQueryMap(ctx.queryParamMap())
//                val events = theDao.retrieve(filter)
//
//                // build the result
//                val result = HashMap<String, Any>()
//                result["events"] = events.map { it -> Event.ModelMapper.asMap(it) }
//                if (filter.pageSize != null || filter.lastId != null) {
//                    result["paging"] = buildPaging(events, filter)
//                }
//                ctx.json(mapOf("payload" to result))

                ctx.json(mapOf("to" to "do"))
            }


        }


    }
}
