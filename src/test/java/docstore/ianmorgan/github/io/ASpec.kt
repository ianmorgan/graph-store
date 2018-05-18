package docstore.ianmorgan.github.io

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith

@RunWith(JUnitPlatform::class)
object ASpec : Spek({

    describe ("a dummy spec") {
        it ("should do something"){
            assert.that(1, equalTo(1))
        }
    }
})