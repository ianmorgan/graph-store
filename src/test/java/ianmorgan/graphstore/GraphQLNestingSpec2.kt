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


        it("should return friends of friends") {

            // test nesting of collections

            val query = """
                {droid(id: "2001"){name,starships{name,model},friends(count:2){name,friendsCount,friends(first: 2){name}}}}
            """.trimIndent()

            val result = graphQL.execute(query)
            val expected = """
                {droid={name=R2-D2, starships=[], friends=[{name=Luke Skywalker, friendsCount=4, friends=[{name=C-3PO}, {name=R2-D2}]}, {name=Han Solo, friendsCount=3, friends=[{name=R2-D2}]}]}}
            """.trimIndent()

            assert.that(result.errors.isEmpty(), equalTo(true))
            assert.that(
                result.getData<Any>().toString(),
                equalTo(expected)
            )
        }

        xit("should return Luke's enemy") {

            // test embedded type linked by ID (aggregation in UML speak)

            val query = """
                {human(id: "1002"){name,enemy{name}}}
            """.trimIndent()

            val result = graphQL.execute(query)
            val expected = """
                {human={name=Han Solo, enemy={name=Darth Vader}}}
            """.trimIndent()

            assert.that(result.errors.isEmpty(), equalTo(true))
            assert.that(
                result.getData<Any>().toString(),
                equalTo(expected)
            )
        }

    }
})