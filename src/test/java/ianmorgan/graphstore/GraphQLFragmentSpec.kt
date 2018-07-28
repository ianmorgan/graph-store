package ianmorgan.graphstore

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import graphql.GraphQL
import ianmorgan.graphstore.dal.DocsDao
import ianmorgan.graphstore.graphql.GraphQLFactory
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import java.io.FileInputStream

/**
 * Fragments should be handled by the graphql-java library. These tests are just
 * to confirm that the basic behaviour has not been affected by the graph-store
 */
@RunWith(JUnitPlatform::class)
object GraphQLFragmentSpec : Spek({

    val starWarSchema = FileInputStream("src/schema/starwars.graphqls").bufferedReader().use { it.readText() }
    lateinit var graphQL : GraphQL

    describe ("To confirm inbuilt graphql-java fragments support working as expected") {

        beforeGroup {
            val docsDao = DocsDao(starWarSchema)
            graphQL = GraphQLFactory.build(starWarSchema,docsDao)
            DataLoader(docsDao).loadDirectory("src/test/resources/starwars")
        }

        it ("should return luke & R2") {

            val query = """
                {
                  leftComparison: droid(id: 2001) {
                    ...comparisonFields
                  }
                  rightComparison: human(id: 1000) {
                    ...comparisonFields
                  }
                }

                fragment comparisonFields on Character {
                  name
                  appearsIn
                  friends {
                       name
                  }
                }
                """

            val result = graphQL.execute(query)

            assert.that(result.errors.isEmpty(), equalTo(true))
            assert.that(result.getData<Any>().toString(),
                equalTo("{leftComparison={name=R2-D2, appearsIn=[NEWHOPE, EMPIRE, JEDI], friends=[{name=Luke Skywalker}, {name=Han Solo}, {name=Leia Organa}]}, rightComparison={name=Luke Skywalker, appearsIn=[NEWHOPE, EMPIRE, JEDI], friends=[{name=Han Solo}, {name=Leia Organa}, {name=C-3PO}, {name=R2-D2}]}}"));
        }
    }
})