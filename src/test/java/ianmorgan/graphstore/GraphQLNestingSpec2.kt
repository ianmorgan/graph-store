package ianmorgan.graphstore

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import graphql.GraphQL
import ianmorgan.graphstore.dal.DocsDao
import ianmorgan.graphstore.graphql.GraphQLFactory
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.xit
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import java.io.FileInputStream

// TODO - these should work once hardcoding of the 'FriendsDataFetcher' is fixed

@RunWith(JUnitPlatform::class)
object GraphQLNestingSpec2 : Spek({

    val starWarSchema = FileInputStream("src/schema/starwars.graphqls").bufferedReader().use { it.readText() }
    lateinit var docsDao: DocsDao
    lateinit var graphQL: GraphQL

    describe("Queries returing nested data") {

        beforeGroup {
            // setup GraphQL & DAO with some initial data
            docsDao = DocsDao(starWarSchema)
            val dataLoader = DataLoader(docsDao)
            dataLoader.loadDirectory("src/test/resources/starwars")

            graphQL = GraphQLFactory.build(starWarSchema, docsDao)
        }


        // TODO - this isn't working
        xit("should return friends of friends ") {
            // testing query, but no nesting
            val query = """
                {droid(id: "2001"){name,friends{name,friends{name}}}}
"""
            val result = graphQL.execute(query)
            val expected = """
                {
  "data" : {
    "droid" : {
      "name" : "R2-D2",
      "friends" : [ {
        "name" : "Luke Skywalker",
        "friends" : "[Han Solo, 1003, 2000, 2001]"
      }, {
        "name" : "Han Solo",
        "friends" : "[Luke Skywalker, 1003, 2001]"
      }, {
        "name" : "Leia Organa",
        "friends" : "[Luke Skywalker, 1002, 2000, 2001]"
      } ]
    }
  }
}
            """.trimIndent()

            assert.that(result.errors.isEmpty(), equalTo(true))
            //  assert.that(
            //      result.getData<Any>().toString(),
            //      equalTo(expected)
            //  )
        }


    }
})