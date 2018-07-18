package ianmorgan.docstore

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import graphql.GraphQL
import ianmorgan.docstore.dal.DocsDao
import ianmorgan.docstore.graphql.GraphQLFactory
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.xdescribe
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import java.io.FileInputStream

// TODO - these should work once hardcoding of the 'FriendsDataFetcher' is fixed

@RunWith(JUnitPlatform::class)
object GraphQLNestingSpec : Spek({

    val schema = FileInputStream("src/schema/nesting.graphqls").bufferedReader().use { it.readText() }
    lateinit var theDao: DocsDao
    lateinit var graphQL: GraphQL

    describe("Queries returing nested data") {

        beforeGroup {
            // setup GraphQL & DAO with some initial data
            theDao = DocsDao(schema)
            val dao = theDao.daoForDoc("Directory")
            dao.store(
                mapOf(
                    "handle" to "root",
                    "name" to "/",
                    "files" to listOf("file1", "file2"),
                    "children" to listOf("dir1")
                )
            )

            dao.store(
                mapOf(
                    "handle" to "dir1",
                    "name" to "dir1",
                    "files" to listOf("a", "b", "c")
                )
            )




            graphQL = GraphQLFactory.build(schema, theDao)

        }

        it("should return the top node with files") {
            // testing query, but no nesting
            val query = """{
                    dir(handle: "root") {
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
            // testing  one level of nesting
            val query = """{
                    dir(handle: "root") {
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

        it("should return no children for a leaf node ") {
            // testing that no children are returned
            val query = """{
                    dir(handle: "dir1") {
                       name,
                       files,
                       children {name}
                    }}
"""
            val result = graphQL.execute(query)

            assert.that(result.errors.isEmpty(), equalTo(true))
            assert.that(
                result.getData<Any>().toString(),
                equalTo("{dir={name=dir1, files=[a, b, c], children=[]}}")
            )
        }



    }
})