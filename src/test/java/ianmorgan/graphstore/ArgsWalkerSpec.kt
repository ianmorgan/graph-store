package ianmorgan.graphstore

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import ianmorgan.github.io.jsonUtils.JsonHelper
import ianmorgan.graphstore.graphql.ArgsWalker
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
object ArgsWalkerSpec : Spek({

    lateinit var app: Javalin
    val baseUrl = "http://localhost:8002/"

    describe("walking the query args") {

            beforeEachTest {}

            it("should walk simple args") {
                val args = mapOf("name" to emptyMap<String,Any>(),
                    "friends" to emptyMap(),
                    "friends/name" to emptyMap())

                val root = ArgsWalker("/", args)

                val friends = root.walkPath("friends")
                val expected = mapOf("name" to emptyMap<String,Any>())

                assert.that(friends.args, equalTo(expected))
            }
        }

})
