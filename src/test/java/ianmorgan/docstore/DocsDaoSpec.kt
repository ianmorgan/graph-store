package ianmorgan.docstore

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import java.io.FileInputStream

@RunWith(JUnitPlatform::class)
object DocsDaoSpec : Spek({

    val starWarSchema = FileInputStream("src/schema/starwars.graphqls").bufferedReader().use { it.readText() }  // defaults to UTF-8



    describe ("A simple DAO ") {

        it ("should have a doc for each type in schema") {
            val dao = DocsDao(starWarSchema)
            assert.that(dao.availableDocs(), equalTo(setOf("Droid","Human")))
        }

        it ("should return be aggregate id "){
            val dao = DocsDao(starWarSchema)
            val r2 = dao.retrieve("2001")
            assert.that("R2-D2", equalTo(r2["name"]))
        }
    }
})