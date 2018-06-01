package ianmorgan.docstore

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import ianmorgan.github.io.jsonUtils.JsonHelper
import io.javalin.Javalin
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import org.eclipse.jetty.util.URIUtil
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.json.JSONObject
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith

@RunWith(JUnitPlatform::class)
object ControllerSpec : Spek({

    lateinit var app: Javalin
    val baseUrl = "http://localhost:8002/"

    describe(" controller") {
        beforeGroup {
            val parser = DefaultParser()
            val cmd = parser.parse(Options(), arrayOf<String>())
            app = JavalinApp(8002, cmd).init()
            println("app started...")
        }

        beforeEachTest {
            // todo
        }

        context("'GET /graphql' behaviour") {
            beforeEachTest {}

            it("should return all events if no filters") {
                val query = URIUtil.encodePath("query={droid(id: \"2001\") {name}}")
                println(query)
                val response = khttp.get(url = baseUrl + "graphql?" + query)
                assert.that(response.statusCode, equalTo(200))

                println(response.text)

                val expectedJson = """
                    {"data":{"droid": {"name":"R2D2"}}}
"""
                val actualAsMap = JsonHelper.jsonToMap(response.jsonObject)
                val expectedAsMap = JsonHelper.jsonToMap(JSONObject(expectedJson))
                assert.that(expectedAsMap, equalTo(actualAsMap))
            }
        }

        context("'POST /docs' behaviour") {

            it("should store a valid doc") {
                val url = baseUrl + "docs/Droid"
                val payload = """
               { "id" : "2001",  "name": "R2-D2","appearsIn": ["NEWHOPE","EMPIRE","JEDI"] }
"""

                // save document
                val response = khttp.post(url, data = JSONObject(payload))
                assert.that(response.statusCode, equalTo(200))
                assert.that(response.jsonObject.toMap().isEmpty(), equalTo(true))

                // check it can be read back
                val readResponse = khttp.get(url = url + "/2001")
                val result = readResponse.jsonObject.getJSONObject("data")

                assert.that(result.getString("name"), equalTo("R2-D2"))
            }

            it("should return an error if the submitted doc is invalid") {
                val url = baseUrl + "docs/Droid"
                val payload = """
                { "id" : "2001",  "rubbish": "data" }
"""
                // try and save the document
                val response = khttp.post(url, data = JSONObject(payload))
                val errors = response.jsonObject.getJSONArray("errors")

                // there should be an error
                assert.that(response.statusCode, equalTo(500))
                assert.that(errors.length(), equalTo(1))
                assert.that(errors.getJSONObject(0).getString("message"),
                    equalTo("Unexpected field rubbish in document "))
            }
        }

        afterEachTest {
            //todo
        }

        afterGroup {
            app.stop()
            println("app stopped")
        }
    }
})
