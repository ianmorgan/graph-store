package ianmorgan.graphstore

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import graphql.GraphQL
import ianmorgan.graphstore.dal.DocDao
import ianmorgan.graphstore.dal.DocsDao
import ianmorgan.graphstore.graphql.GraphQLFactory
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import java.io.FileInputStream

@RunWith(JUnitPlatform::class)
object GraphQLTempSpec : Spek({

    val starWarSchema = FileInputStream("src/schema/starwars.graphqls").bufferedReader().use { it.readText() }
    lateinit var docsDao: DocsDao
    lateinit var graphQL : GraphQL

    describe ("A place for investigating broken tests") {

        beforeGroup {
            // setup GraphQL & DAO with some initial data
            // see https://github.com/graphql/graphql-js/blob/master/src/__tests__/starWarsData.js
            docsDao = DocsDao(starWarSchema)
            val droidDao = docsDao.daoForDoc("Droid") as DocDao
            droidDao.store(mapOf("id" to "2001",
                "name" to "R2-D2",
                "appearsIn" to listOf("NEWHOPE","EMPIRE","JEDI"),
                "primaryFunction" to "Astromech"))
            droidDao.store( mapOf(  "id" to "2002",
                "name" to "C-3PO",
                "appearsIn" to listOf("NEWHOPE", "EMPIRE", "JEDI"),
                "primaryFunction" to "Protocol Droid"))
            val humanDao = docsDao.daoForDoc("Human") as DocDao
            humanDao.store( mapOf(  "id" to "1000",
                "name" to "Luke Skywalker",
                "appearsIn" to listOf("NEWHOPE", "EMPIRE", "JEDI"),
                "homePlanet" to "Tatooine",
                "friends" to listOf("1002") ))
            humanDao.store( mapOf(  "id" to "1002",
                "name" to "Han Solo",
                "appearsIn" to listOf("NEWHOPE", "EMPIRE", "JEDI"),
                "friends" to listOf("1000") ))


            graphQL = GraphQLFactory.build(starWarSchema,docsDao)

        }


        it ("should return friends for Luke") {

            val query = """{
                    human(id: "1000") {
                       name,
                       friends { name }
                    }}
"""
            val result = graphQL.execute(query)

            assert.that(result.errors.isEmpty(), equalTo(true))
            assert.that(result.getData<Any>().toString(),
                equalTo("{human={name=Luke Skywalker, friends=[{name=Han Solo}]}}"))
        }



    }
})