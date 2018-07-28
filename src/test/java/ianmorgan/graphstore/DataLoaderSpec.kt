package ianmorgan.graphstore

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import ianmorgan.graphstore.dal.DocDao
import ianmorgan.graphstore.dal.DocsDao
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import java.io.File

@RunWith(JUnitPlatform::class)
object DataLoaderSpec : Spek({

    val starWarSchema = File("src/schema/starwars.graphqls")

    describe ("a simple dataloader ") {

        it ("should load the starwars example data") {
            val dao = DocsDao.fromSchema(starWarSchema)
            val dataLoader = DataLoader(dao)

            dataLoader.loadDirectory("src/test/resources/starwars")
            assert.that((dao.daoForDoc("Human") as DocDao).count(), equalTo(5))
            assert.that((dao.daoForDoc("Droid") as DocDao).count(), equalTo(2))
        }
    }
})