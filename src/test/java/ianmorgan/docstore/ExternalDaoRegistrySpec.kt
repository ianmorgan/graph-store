package ianmorgan.docstore

import com.natpryce.hamkrest.equalTo
import ianmorgan.docstore.dal.ExternalDaoRegistry
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith

@RunWith(JUnitPlatform::class)
object ExternalDaoRegistrySpec : Spek({


    describe ("Building a registry from events") {



        it ("should register a starship DAO") {
            // create registry
            val registry = ExternalDaoRegistry()

            // register the DAO
            val mapper = """
                import ianmorgan.docstore.mapper.MapperHelper;

                def helper = new MapperHelper(raw)
                helper.copyIfExists('name')
                return helper.output() """.trimIndent()
            val config = mapOf("baseUrl" to "https://swapi.co/api/starships/",
                "resultMapperScript" to mapper)

            registry.registerDao("Starship")
            registry.configureDao("Starship",config)
            registry.rebuildDaos()

            // load the DAO
            val dao = registry.daoForDoc("Starship")

            // does it work ?
            val result = dao.retrieve("10")
            val expected = mapOf ("id" to "10", "name" to "Millennium Falcon"
            ) as Map<String,Any>

            com.natpryce.hamkrest.assertion.assert.that(result, equalTo(expected))
        }

    }
})