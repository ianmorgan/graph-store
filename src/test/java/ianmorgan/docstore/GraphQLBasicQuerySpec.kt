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
            graphQL = GraphQLFactory2.build(starWarSchema,docsDao)

        }

        it ("query for a Droid by id") {

            val query = """{
                    droid(id: "2001") {
                       name,appearsIn
                    }}
"""
            val result = graphQL.execute(query)

            assert.that(result.errors.isEmpty(), equalTo(true))
            assert.that(result.getData<Any>().toString(),
                equalTo("{droid={name=R2-D2, appearsIn=[NEWHOPE, EMPIRE, JEDI]}}"))

        }
    }
})