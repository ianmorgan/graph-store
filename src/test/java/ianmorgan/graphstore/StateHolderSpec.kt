package ianmorgan.graphstore

import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assert
import ianmorgan.graphstore.dal.InMemoryEventStore
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import java.io.File

@RunWith(JUnitPlatform::class)
object StateHolderSpec : Spek({

    val starWarSchema = File("src/schema/starwars.graphqls")

    val stateHolder = StateHolder(InMemoryEventStore())

    describe ("the StateHolder singleton ") {

        it ("should build with a valid schema") {
            val result = stateHolder.build(starWarSchema, HashMap())
            assert.that(stateHolder.isValid() , equalTo(true))
            assert.that(result, equalTo(true))
            assert.that(stateHolder.exception() , absent())
            assert.that(stateHolder.docsDao().availableDocs() , equalTo(setOf("Human","Droid")))
        }

        it ("should have 'invalid' state if a bad schema") {
            val result = stateHolder.build("a rubbish schema",HashMap())
            assert.that(stateHolder.isValid() , equalTo(false))
            assert.that(result, equalTo(false))
            assert.that(stateHolder.exception(), present())
            assert.that(stateHolder.exception()!!.message, equalTo("errors=[InvalidSyntaxError{ message=Invalid Syntax ,locations=[SourceLocation{line=1, column=0}]}]"))
        }
    }
})