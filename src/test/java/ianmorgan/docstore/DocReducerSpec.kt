package ianmorgan.docstore

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import ianmorgan.docstore.dal.DocReducer
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import java.util.*

@RunWith(JUnitPlatform::class)
object DocReducerSpec : Spek({

    describe ("reducing doc events to recreate state") {

        it ("should reduce a single event to itself") {
            // setup
            val event = mapOf<String,Any>("name" to "Luke")

            // verify
            val result = DocReducer.reduceEvents(listOf(Helper.buildEvent(event)))
            assert.that(result, equalTo(event))
        }

        it ("should apply update to preceding state") {
            // setup
            val ev1 = mapOf<String,Any>("name" to "Luke", "homePlanet" to "Tatooine")
            val ev2 = mapOf("name" to "Luke Skywalker", "appearsIn" to listOf("NEWHOPE"))

            // verify
            val expected =  mapOf<String,Any>("name" to "Luke Skywalker",
                "homePlanet" to "Tatooine",
                "appearsIn" to listOf("NEWHOPE"))
            val result = DocReducer.reduceEvents(Helper.buildEvents(listOf(ev1, ev2)))
            assert.that(result, equalTo(expected))
        }

        it ("should remove updated fields set to null") {
            // setup
            val ev1 = mapOf<String,Any?>("name" to "Luke", "homePlanet" to "Tatooine")
            val ev2 = mapOf<String,Any?>("name" to "Luke Skywalker", "homePlanet" to null)

            // verify
            val expected =  mapOf<String,Any>("name" to "Luke Skywalker")
            val result = DocReducer.reduceEvents(Helper.buildEvents(listOf(ev1, ev2)))
            assert.that(result, equalTo(expected))
        }

    }
}


)

private object Helper {
    fun buildEvent(payload: Map<String, Any?>): Map<String, Any> {
        val ev = HashMap<String, Any>()
        ev["type"] = "DocUpdated"
        ev["id"] = UUID.randomUUID().toString()
        ev["timestamp"] = System.currentTimeMillis()
        ev["creator"] = "doc-store"
        ev["payload"] = payload
        return ev
    }
    fun buildEvents(raw : List<Map<String,Any?>>) : List<Map<String, Any>> {
        return raw.map { it -> buildEvent(it) }
    }

}