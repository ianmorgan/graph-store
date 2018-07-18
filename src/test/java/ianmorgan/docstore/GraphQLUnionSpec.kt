package ianmorgan.docstore

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import graphql.GraphQL
import ianmorgan.docstore.dal.DocsDao
import ianmorgan.docstore.graphql.GraphQLFactory
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import java.io.FileInputStream


@RunWith(JUnitPlatform::class)
object GraphQLUnionSpec : Spek({

    val starWarSchema = FileInputStream("src/schema/starwars.graphqls").bufferedReader().use { it.readText() }
    lateinit var docsDao: DocsDao
    lateinit var graphQL: GraphQL

    describe("Query on union of Human and Droid") {

        beforeGroup {
            // setup GraphQL & DAO with some initial data
            // see https://github.com/graphql/graphql-js/blob/master/src/__tests__/starWarsData.js
            docsDao = DocsDao(starWarSchema)
            val dataLoader = DataLoader(docsDao)
            dataLoader.loadDirectory("src/test/resources/starwars")

            graphQL = GraphQLFactory.build(starWarSchema, docsDao)

        }

        it("should return type data for a Human") {

            val query = """{
  search(name_contains: "d") {
    ... on Droid {
      name,
      primaryFunction
    }
    ... on Human {
       name
       appearsIn
    }
  }
}"""

            val result = graphQL.execute(query)

            assert.that(result.errors.isEmpty(), equalTo(true))

            val expected = "{search=[{name=Darth Vader, appearsIn=[NEWHOPE, EMPIRE, JEDI]}, {name=R2-D2, primaryFunction=Astromech}]}"
            assert.that(result.getData<Any>().toString(), equalTo(expected))
        }


    }
})