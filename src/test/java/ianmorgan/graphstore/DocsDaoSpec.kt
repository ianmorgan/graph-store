package ianmorgan.graphstore

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import ianmorgan.graphstore.dal.DocsDao
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import java.io.File

@RunWith(JUnitPlatform::class)
object DocsDaoSpec : Spek({

    val starWarSchema = File("src/schema/starwars.graphqls")

    describe ("A simple DAO ") {

        it ("should have a DocDao for each type in schema") {
            val dao = DocsDao.fromSchema(starWarSchema)
            assert.that(dao.availableDocs(), equalTo(setOf("Droid","Human")))
        }

        it ("should have an InterfaceDao for each interface in the schema") {
            val dao = DocsDao.fromSchema(starWarSchema)
            assert.that(dao.availableInterfaces(), equalTo(setOf("Character")))
        }
    }
})