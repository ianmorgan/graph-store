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
object GraphQLFailingQuerySpec : Spek({

    val starWarSchema = FileInputStream("src/schema/starwars.graphqls").bufferedReader().use { it.readText() }
    lateinit var docsDao: DocsDao
    lateinit var graphQL : GraphQL

    describe ("Queries that are expected to fail for some reason") {

        beforeGroup {
            docsDao = DocsDao(starWarSchema)
            val dataLoader = DataLoader(docsDao)
            dataLoader.loadDirectory("src/test/resources/starwars")

            graphQL = GraphQLFactory.build(starWarSchema, docsDao)

        }

        it ("should return null if querying for a Droid using the ID for Luke") {

            val query = """{
                    droid(id: "1001") {name}}
"""
            val result = graphQL.execute(query)

            assert.that(result.errors.isEmpty(), equalTo(true))
            assert.that(result.getData<Any>().toString(),
                equalTo("{droid=null}"))
        }


    }
})