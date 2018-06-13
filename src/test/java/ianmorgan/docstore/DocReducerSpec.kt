package ianmorgan.docstore

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.throws
import ianmorgan.docstore.dal.DocReducer
import ianmorgan.docstore.dal.DocsDao
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import java.io.File

@RunWith(JUnitPlatform::class)
object DocReducerSpec : Spek({

    describe ("reducing doc events to recreate state") {

        it ("should reduce single event to itself") {
            val event = mapOf<String,Any>("name" to "Luke")
            val result = DocReducer.reduceEvents(listOf(event))

            assert.that(result, equalTo(event))
        }

        it ("should apply partial update to original state") {
            val ev1 = mapOf<String,Any>("name" to "Luke", "homePlanet" to "Tatooine")
            val ev2 = mapOf("name" to "Luke Skywalker", "appearsIn" to listOf("NEWHOPE"))

            val expected =  mapOf<String,Any>("name" to "Luke Skywalker",
                "homePlanet" to "Tatooine",
                "appearsIn" to listOf("NEWHOPE"))
            assert.that(DocReducer.reduceEvents(listOf(ev1,ev2)), equalTo(expected))
        }

        it ("should remove uodated fields set to null") {
            val ev1 = mapOf<String,Any?>("name" to "Luke", "homePlanet" to "Tatooine")
            val ev2 = mapOf<String,Any?>("name" to "Luke Skywalker", "homePlanet" to null)

            val result = DocReducer.reduceEvents(listOf(ev1,ev2))
            val expected =  mapOf<String,Any>("name" to "Luke Skywalker")
            assert.that(result, equalTo(expected))
        }

    }
})