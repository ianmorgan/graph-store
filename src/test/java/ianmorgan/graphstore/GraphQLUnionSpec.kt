package ianmorgan.graphstore

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import graphql.GraphQL
import ianmorgan.graphstore.dal.DocsDao
import ianmorgan.graphstore.dal.EventStoreClient
import ianmorgan.graphstore.dal.ExternalDaoRegistry
import ianmorgan.graphstore.dal.InMemoryEventStore
import ianmorgan.graphstore.graphql.GraphQLFactory
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import java.io.FileInputStream


@RunWith(JUnitPlatform::class)
object GraphQLUnionSpec : Spek({

    val starWarSchema = FileInputStream("src/schema/starwars_ex.graphqls").bufferedReader().use { it.readText() }
    lateinit var docsDao: DocsDao
    lateinit var graphQL: GraphQL

    describe("Query on union of Human and Droid") {

        beforeGroup {
            var es: EventStoreClient = InMemoryEventStore()

            // register external DAO
            val externalDaoRegistry = ExternalDaoRegistry(es)
            JavalinApp.AppHelper.registerStarshipDao(externalDaoRegistry)
            externalDaoRegistry.rebuildDaos()

            // setup DAOs and test data
            docsDao = DocsDao(starWarSchema,es, externalDaoRegistry.allDaos())
            val dataLoader = DataLoader(docsDao)
            dataLoader.loadDirectory("src/test/resources/starwars_ex")

            graphQL = GraphQLFactory.build(starWarSchema, docsDao)

        }

        it("should build union of Darth Vader & R2D2") {

            // make sure this triggers nesting of fields to prove "walking"
            // of internal structure (which needs custom logic)
            val query = """{
  search(name_contains: "d") {
    ... on Droid {
      name,
      primaryFunction

    }
    ... on Human {
       name,
       appearsIn,
       friends{name}
    }
  }
}"""

            val result = graphQL.execute(query)

            assert.that(result.errors.isEmpty(), equalTo(true))

            //val expected = "{search=[{name=Darth Vader, appearsIn=[NEWHOPE, EMPIRE, JEDI], friends=[{name=Wilhuff Tarkin}]}, {name=R2-D2, primaryFunction=Astromech, starships=[{name=X-wing}]}]}"
            val expected = "{search=[{name=Darth Vader, appearsIn=[NEWHOPE, EMPIRE, JEDI], friends=[{name=Wilhuff Tarkin}]}, {name=R2-D2, primaryFunction=Astromech}]}"

            assert.that(result.getData<Any>().toString(), equalTo(expected))
        }


    }
})

