package ianmorgan.graphstore

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import graphql.GraphQL
import ianmorgan.graphstore.dal.DocsDao
import ianmorgan.graphstore.graphql.GraphQLFactory
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.xit
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import java.io.FileInputStream

// TODO - these should work once hardcoding of the 'FriendsDataFetcher' is fixed

@RunWith(JUnitPlatform::class)
object GraphQLNestingSpec2 : Spek({

    val starWarSchema = FileInputStream("src/schema/starwars_ex.graphqls").bufferedReader().use { it.readText() }
    lateinit var docsDao: DocsDao
    lateinit var graphQL: GraphQL

    describe("Queries returing nested data") {

        beforeGroup {
            // setup GraphQL & DAO with some initial data
            docsDao = DocsDao(starWarSchema)
            val dataLoader = DataLoader(docsDao)
            dataLoader.loadDirectory("src/test/resources/starwars_ex")

            graphQL = GraphQLFactory.build(starWarSchema, docsDao)
        }


        // TODO - this isn't working
        it("should return friends of friends ") {
            // testing query, but no nesting
            val query = """
                {droid(id: "2001"){name,starships{name,model},friends{name,friends(first: 2){name}}}}

"""
            val result = graphQL.execute(query)
            val expected = """
               {droid={name=R2-D2, starships=[], friends=[{name=Leia Organa, friends=[{name=Luke Skywalker}, {name=Han Solo}, {name=C-3PO}, {name=R2-D2}]}]}}
            """.trimIndent()

            assert.that(result.errors.isEmpty(), equalTo(true))
            assert.that(
                  result.getData<Any>().toString(),
                  equalTo(expected)
              )
        }

    }
})