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
 * Introspection should be handled by the graphql-java library. These tests are just
 * to confirm that the basic behaviour has not been affected by the graph-store
 */
@RunWith(JUnitPlatform::class)
object GraphQLIntrospectionSpec : Spek({

    val starWarSchema = FileInputStream("src/schema/starwars.graphqls").bufferedReader().use { it.readText() }
    lateinit var graphQL : GraphQL

    describe ("To confirm inbuilt graphql-java introspection API working as expected") {

        beforeGroup {
            graphQL = GraphQLFactory.build(starWarSchema,DocsDao(starWarSchema))

        }

        it ("should return type data for a Human") {

            val query = """query {
                    __type(name: "Human") {
                        kind
                        name
                        fields {
                            name
                            description
                            type {
                                name
                            }
                        }
                    }
                }"""

            val result = graphQL.execute(query)


            assert.that(result.errors.isEmpty(), equalTo(true))

            assert.that(result.getData<Any>().toString(),
                equalTo("{__type={kind=OBJECT, name=Human, fields=[{name=id, description=null, type={name=null}}, {name=name, description=null, type={name=null}}, {name=friends, description=null, type={name=null}}, {name=friendsCount, description=null, type={name=Int}}, {name=appearsIn, description=null, type={name=null}}, {name=homePlanet, description=null, type={name=String}}]}}"))

        }
    }
})