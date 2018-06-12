package ianmorgan.docstore

import com.natpryce.hamkrest.absent
import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.isNullOrBlank
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import java.io.File

@RunWith(JUnitPlatform::class)
object InterfaceDaoSpec : Spek({

    val starWarSchema = File("src/schema/starwars.graphqls")
    lateinit var docsDao: DocsDao

    describe("An InterfaceDao ") {

        beforeGroup {
            docsDao = DocsDao.fromSchema(starWarSchema)
            val droidDao = docsDao.daoForDoc("Droid")
            droidDao.store(
                mapOf(
                    "id" to "2001",
                    "name" to "R2-D2",
                    "appearsIn" to listOf("NEWHOPE", "EMPIRE", "JEDI"),
                    "primaryFunction" to "Astromech"
                )
            )
            val humanDao = docsDao.daoForDoc("Human")
            humanDao.store(
                mapOf(
                    "id" to "1000",
                    "name" to "Luke Skywalker",
                    "appearsIn" to listOf("NEWHOPE", "EMPIRE", "JEDI"),
                    "homePlanet" to "Tatooine",
                    "friends" to listOf("2001")
                )
            )

        }

        it("should query on the Character interface for Luke") {
            val interfaceDao = docsDao.daoForInterface("Character")
            val luke = interfaceDao.retrieve("1000")!!

            val expected = mapOf(
                "id" to "1000",
                "name" to "Luke Skywalker",
                "appearsIn" to listOf("NEWHOPE", "EMPIRE", "JEDI"),
                "friends" to listOf("2001")
            )
            assert.that(luke, equalTo(expected))
        }

        it("should return null if no Character found") {
            val interfaceDao = docsDao.daoForInterface("Character")
            val result = interfaceDao.retrieve("missing")

            assert.that(result, absent())
        }

    }
})