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

        context("GET /graphql specs") {
            beforeEachTest {}

            it("should return all events if no filters") {
                val query = URIUtil.encodePath("query={character(name: \"homer\") {hairColour}}")
                println (query)
                val response = khttp.get(url = baseUrl + "graphql?"+query )
                assert.that(response.statusCode, equalTo(200))

                println (response.text)


                val expectedJson = """
                    {"data":{"character":{"hairColour":"bald!"}}}
"""
                val actualAsMap = JsonHelper.jsonToMap(response.jsonObject)
                val expectedAsMap = JsonHelper.jsonToMap(JSONObject(expectedJson))
                assert.that(expectedAsMap, equalTo(actualAsMap))
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
