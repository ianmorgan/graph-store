package ianmorgan.docstore

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.contains
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.startsWith
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
object SchemaControllerSpec : Spek({

    lateinit var app: Javalin
    val baseUrl = "http://localhost:8002/"

    describe("the schema controller") {
        beforeGroup {
            val parser = DefaultParser()
            val cmd = parser.parse(Options(), arrayOf<String>())
            app = JavalinApp(8002, cmd).init()
            println("app started...")
        }


        context("'GET /schema' behaviour") {
            beforeEachTest {}

            it("should return the schema as text") {
                val response = khttp.get(url = baseUrl + "schema" )
                assert.that(response.statusCode, equalTo(200))
                assert.that(response.headers["content-type"], equalTo("text/plain;charset=utf-8"))
                assert.that(response.text, startsWith("# The query type, represents all of the entry points into our object graph"))
            }
        }

        context("'POST /schema' behaviour") {

            it("should store a valid schema") {
                val url = baseUrl + "schema"
                val schema= """
                    type Query {
                        human(id: ID!): Human
                    }

                    type Human {
                        id: ID!
                        name: String!
                    }
"""
                // save document
                val response = khttp.post(url, data = schema)
                assert.that(response.statusCode, equalTo(200))
                assert.that(response.jsonObject.toMap().isEmpty(), equalTo(true))

                // check it can be read back
                val readResponse = khttp.get(url = url )

                assert.that(readResponse.text, equalTo(schema))
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
