package ianmorgan.docstore

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import graphql.GraphQL
import ianmorgan.docstore.dal.DocsDao
import ianmorgan.docstore.graphql.GraphQLFactory2
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import java.io.FileInputStream

@RunWith(JUnitPlatform::class)
object GraphQLPaginationSpec : Spek({

    val starWarSchema = FileInputStream("src/schema/starwars.graphqls").bufferedReader().use { it.readText() }
    lateinit var docsDao: DocsDao
    lateinit var graphQL : GraphQL

    describe ("Some simple scalar queries") {

        beforeGroup {
            // setup GraphQL & DAO with some initial data
            // see https://github.com/graphql/graphql-js/blob/master/src/__tests__/starWarsData.js
            docsDao = DocsDao(starWarSchema)
            val dataLoader = DataLoader(docsDao)
            dataLoader.loadDirectory("src/test/resources/starwars")

            graphQL = GraphQLFactory2.build(starWarSchema,docsDao)

        }

        it ("should return friends count") {

            val query = """{
                    droid(id: "2001") {friendsCount,friends { name }}}
"""
            val result = graphQL.execute(query)

            assert.that(result.errors.isEmpty(), equalTo(true))
            assert.that(result.getData<Any>().toString(),
                equalTo("{droid={friendsCount=3, friends=[{name=Luke Skywalker}, {name=Han Solo}, {name=Leia Organa}]}}"))
        }


        it ("should limit friend collection using 'start' param") {

            val query = """{
                    droid(id: "2001") {friendsCount,friends(first: 2) { name }}}
"""
            val result = graphQL.execute(query)

            assert.that(result.errors.isEmpty(), equalTo(true))
            assert.that(result.getData<Any>().toString(),
                equalTo("{droid={friendsCount=3, friends=[{name=Leia Organa}]}}"))
        }


        it ("should limit friend collection using 'count' param") {

            val query = """{
                    droid(id: "2001") {friendsCount,friends(count: 2) { name }}}
"""
            val result = graphQL.execute(query)

            assert.that(result.errors.isEmpty(), equalTo(true))
            assert.that(result.getData<Any>().toString(),
                equalTo("{droid={friendsCount=3, friends=[{name=Luke Skywalker}, {name=Han Solo}]}}"))
        }

        it ("should return empty list for 'count' param of 0") {

            val query = "{droid(id: \"2001\") {friends(count: 0) { name }}}"
            val result = graphQL.execute(query)

            assert.that(result.errors.isEmpty(), equalTo(true))
            assert.that(result.getData<Any>().toString(),
                equalTo("{droid={friends=[]}}"))
        }

        it ("should return empty list if 'first' param larger than list") {

            val query = "{droid(id: \"2001\") {friends(first: 4) { name }}}"
            val result = graphQL.execute(query)

            assert.that(result.errors.isEmpty(), equalTo(true))
            assert.that(result.getData<Any>().toString(),
                equalTo("{droid={friends=[]}}"))
        }

        it ("should return full list if 'count' param larger than list") {

            val query = "{droid(id: \"2001\") {friends(count: 4) { name }}}"
            val result = graphQL.execute(query)

            assert.that(result.errors.isEmpty(), equalTo(true))
            assert.that(result.getData<Any>().toString(),
                equalTo("{droid={friends=[{name=Luke Skywalker}, {name=Han Solo}, {name=Leia Organa}]}}"))
        }


    }
})