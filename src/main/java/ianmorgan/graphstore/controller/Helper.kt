package ianmorgan.graphstore.controller

import groovy.lang.Binding
import groovy.lang.GroovyShell
import io.javalin.Context
import java.io.FileInputStream

class CtxHelper constructor(context : Context){
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


    fun isHTMLResponseExpected() : Boolean {

        //return false;

        // explicit with query param
        if ("html".equals(ctx.queryParam("responseType"))) return true
        if ("json".equals(ctx.queryParam("responseType"))) return false

        // check accept header
        val accept = ctx.request().getHeader("accept")
        if (accept != null) {
            val acceptTypes = accept.split(",")

            // can never be HTML if JSON specified as an accept type
            for (type in acceptTypes) {
                println(type)
                if (type == "application/json") return false;
            }

            // have explicitly asked for HTML encoding
            for (type in acceptTypes) {
                println(type)
                if (type == "html/text") return true;
            }
        }


        // final test - was the original request from a form submission ?
        if ("application/x-www-form-urlencoded" == ctx.request().getHeader("content-type")) {
            return true;
        }


        // default is to return JSON
        return false;
    }

    fun renderErrorPage(error : Map<String,Any>) {

        if (Helper.build(ctx).isHTMLResponseExpected()){
            ctx.renderMustache("/errorPage.html", error)
        }
        else {
            ctx.json(error)
        }
    }

}
object Helper{

    fun build(ctx : Context) : CtxHelper {
        return CtxHelper(ctx)
    }

}