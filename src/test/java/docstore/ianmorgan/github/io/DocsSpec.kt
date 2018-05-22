package docstore.ianmorgan.github.io

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith

@RunWith(JUnitPlatform::class)
object DocsSpec : Spek({

    describe ("A simple DAO ") {
        it ("should return be aggregate id "){
            val dao = DocsDao()
            val r2 = dao.retrieve("2001")
            assert.that("R2-D2", equalTo(r2["name"]))
        }
    }
})