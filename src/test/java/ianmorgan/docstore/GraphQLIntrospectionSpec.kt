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
object GraphQLIntrospectionSpec : Spek({

    val starWarSchema = FileInputStream("src/schema/starwars.graphqls").bufferedReader().use { it.readText() }
    lateinit var docsDao: DocsDao
    lateinit var graphQL : GraphQL

    describe ("Some basic tests to confirm introspection API") {

        beforeGroup {
            graphQL = GraphQLFactory2.build(starWarSchema,DocsDao(starWarSchema))

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

            // TODO - we are not returning all the data necessary here, in particular type information is null
            assert.that(result.getData<Any>().toString(),
                equalTo("{__type={kind=OBJECT, name=Human, fields=[{name=id, description=null, type={name=null}}, {name=name, description=null, type={name=null}}, {name=friends, description=null, type={name=null}}, {name=appearsIn, description= TODO - should be Character, but the resolver isn't yet smart enough to deal with interfaces here, type={name=null}}, {name=homePlanet, description=null, type={name=String}}]}}"))
        }





    }
})