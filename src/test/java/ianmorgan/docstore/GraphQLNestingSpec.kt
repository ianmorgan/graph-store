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
object GraphQLNestingSpec : Spek({

    val allScalarTypes = FileInputStream("src/schema/nesting.graphqls").bufferedReader().use { it.readText() }
    lateinit var theDao: DocsDao
    lateinit var graphQL: GraphQL

    describe("Queries returing nested data") {

        beforeGroup {
            // setup GraphQL & DAO with some initial data
            theDao = DocsDao(allScalarTypes)
            val dao = theDao.daoForDoc("Directory")
            dao.store(
                mapOf(
                    "id" to "root",
                    "name" to "/",
                    "files" to listOf("file1", "file2"),
                    "children" to listOf("dir1")
                )
            )

            dao.store(
                mapOf(
                    "id" to "dir1",
                    "name" to "dir1",
                    "files" to listOf("a", "b", "c")
                )
            )




            graphQL = GraphQLFactory2.build(allScalarTypes, theDao)

        }

        it("should return the top node with files") {

            val query = """{
                    dir(id: "root") {
                       name,files
                    }}
"""
            val result = graphQL.execute(query)

            assert.that(result.errors.isEmpty(), equalTo(true))
            assert.that(
                result.getData<Any>().toString(),
                equalTo("{dir={name=/, files=[file1, file2]}}")
            )
        }

        it("should return the top node with children") {

            val query = """{
                    dir(id: "root") {
                       name,
                       children {name}
                    }}
"""
            val result = graphQL.execute(query)

            assert.that(result.errors.isEmpty(), equalTo(true))
            assert.that(
                result.getData<Any>().toString(),
                equalTo("{dir={name=/, children=[{name=dir1}]}}")
            )
        }



    }
})