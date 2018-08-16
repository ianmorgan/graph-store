package ianmorgan.graphstore.controller

import graphql.validation.ValidationError
import groovy.lang.Binding
import groovy.lang.GroovyShell
import io.javalin.Context
import io.javalin.translator.json.JavalinJacksonPlugin
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

        // explicit with query param
        if ("html".equals(ctx.queryParam("responseType"))) return true
        if ("json".equals(ctx.queryParam("responseType"))) return false

        // check accept header
        val accept = ctx.request().getHeader("accept")
        if (accept != null) {
            val acceptTypes = accept.split(",")

            // can never be HTML if JSON specified as an accept type
            for (type in acceptTypes) {
                if (type == "application/json") return false;
            }

            // have explicitly asked for HTML encoding
            for (type in acceptTypes) {
                if (type == "html/text") return true;
            }
        }


        // final test - was the original request from a form submission ?
        if ("application/x-www-form-urlencoded" == ctx.request().getHeader("content-type")) {
            if ("html".equals(ctx.formParam("responseType"))) return true
            if ("json".equals(ctx.formParam("responseType"))) return false
            // defaults to HTML for <FORM>
            return true;
        }


        // default is to return JSON
        return false;
    }

    /**
     * Renders errors nicely in either HTML or JSON format
     */
    fun renderErrorPage(data : Map<String,Any>) {

        if (Helper.build(ctx).isHTMLResponseExpected()){
            val presentableErrors = ArrayList<Map<String,Any>>()

            val rawErrors = data.get("errors") as List<Any>
            for (rawError in rawErrors){
                val presentableError = HashMap<String,Any>()
                if (rawError is ValidationError) {
                    presentableError["message"] = rawError.message
                    if (rawError.locations.isNotEmpty()) {
                        presentableError["hasLocations"] = true
                        presentableError["locations"] = rawError.locations
                    }
                }
                if (rawError is Map<*,*>){
                    presentableError["message"] = rawError["message"] as String
                    if (rawError.containsKey("stackTrace")){
                        presentableError["hasStackTrace"] = true
                        presentableError["stackTrace"] = rawError["stackTrace"] as Any
                    }
                }

                presentableErrors.add(presentableError)

            }

            ctx.renderMustache("/errorPage.html", mapOf("errors" to  presentableErrors))
        }
        else {
            ctx.json(data)
        }
    }

    fun renderResultsPage(data : Map<String,Any>) {
        if (Helper.build(ctx).isHTMLResponseExpected()) {
            val json = JavalinJacksonPlugin.toJson(data)
            ctx.renderMustache("/resultsPage.html", mapOf("json" to  json))
        } else {
            ctx.json(data)
        }
    }
}


object Helper{

    fun build(ctx : Context) : CtxHelper {
        return CtxHelper(ctx)
    }

}