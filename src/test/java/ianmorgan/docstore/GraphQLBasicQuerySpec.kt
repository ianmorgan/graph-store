package ianmorgan.docstore

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import graphql.GraphQL
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import java.io.FileInputStream

@RunWith(JUnitPlatform::class)
object GraphQLBasicQuerySpec : Spek({

    val starWarSchema = FileInputStream("src/schema/starwarsSimple.graphqls").bufferedReader().use { it.readText() }
    lateinit var docsDao: DocsDao
    lateinit var graphQL : GraphQL

    describe ("Some simple scalar queries") {

        beforeGroup {
            // setup GraphQL & DAO with some initial data
            // see https://github.com/graphql/graphql-js/blob/master/src/__tests__/starWarsData.js
            docsDao = DocsDao(starWarSchema)
            val droidDao = docsDao.daoForDoc("Droid")
            droidDao.store(mapOf("id" to "2001",
                "name" to "R2-D2",
                "appearsIn" to listOf("NEWHOPE","EMPIRE","JEDI"),
                "primaryFunction" to "Astromech"))
            droidDao.store( mapOf(  "id" to "2002",
                "name" to "C-3PO",
                "appearsIn" to listOf("NEWHOPE", "EMPIRE", "JEDI"),
                "primaryFunction" to "Protocol Droid"))
            val humanDao = docsDao.daoForDoc("Human")
            humanDao.store( mapOf(  "id" to "1000",
                "name" to "Luke Skywalker",
                "appearsIn" to listOf("NEWHOPE", "EMPIRE", "JEDI"),
                "homePlanet" to "Tatooine",
                "friends" to listOf("1002") ))
            humanDao.store( mapOf(  "id" to "1002",
                "name" to "Han Solo",
                "appearsIn" to listOf("NEWHOPE", "EMPIRE", "JEDI"),
                "friends" to listOf("1000") ))


            graphQL = GraphQLFactory2.build(starWarSchema,docsDao)

        }

        it ("should query for a Droid by id") {

            val query = """{
                    droid(id: "2001") {
                       name,appearsIn,primaryFunction
                    }}
"""
            val result = graphQL.execute(query)

            assert.that(result.errors.isEmpty(), equalTo(true))
            assert.that(result.getData<Any>().toString(),
                equalTo("{droid={name=R2-D2, appearsIn=[NEWHOPE, EMPIRE, JEDI], primaryFunction=Astromech}}"))
        }


        it ("return null result if Droid not found") {

            val query = """{
                    droid(id: "not a valid id") {
                       name
                    }}
"""
            val result = graphQL.execute(query)

            assert.that(result.errors.isEmpty(), equalTo(true))
            assert.that(result.getData<Any>().toString(),
                equalTo("{droid=null}"))
        }

        it ("should query for Human by id") {

            val query = """{
                    human(id: "1000") {
                       name
                    }}
"""
            val result = graphQL.execute(query)

            assert.that(result.errors.isEmpty(), equalTo(true))
            assert.that(result.getData<Any>().toString(),
                equalTo("{human={name=Luke Skywalker}}"))
        }

        it ("should query for Character") {

            val query = """{
                    character(id: "1000") {
                       name
                    }}
"""
            val result = graphQL.execute(query)

            assert.that(result.errors.isEmpty(), equalTo(true))
            assert.that(result.getData<Any>().toString(),
                equalTo("{character={name=Luke Skywalker}}"))
        }
    }
})