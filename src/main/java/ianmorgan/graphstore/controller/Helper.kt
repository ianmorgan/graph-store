package ianmorgan.graphstore.controller

import io.javalin.Context

class CxtHelper constructor(context : Context){
    val ctx = context;

    /**
     * The payload can either be passed as the body to the request (typical
     * for REST clients and when using tools such as a curl or as a form param (typical
     * for simple UIs).
     *
     */
    fun extractPayload(formFields: List<String> = listOf("payload")): String {
        for (field in formFields) {
            if (ctx.formParamMap().containsKey(field)) {
                return ctx.formParam(field)!!
            }
        }
        return ctx.body()
    }


    /**
     * Try the 2 possible ways of passing a param, query param and form param
     * in order or return the supplied default.
     *
     * Allows for flexible use by clients depending on their design.
     */
    fun extractParam(paramName: String) : String? {
        var param = ctx.queryParam(paramName)
        if (param == null){
            param = ctx.formParam(paramName)
        }
        return param
    }
}
object Helper{

    fun build(ctx : Context) : CxtHelper {
        return CxtHelper(ctx)
    }

}