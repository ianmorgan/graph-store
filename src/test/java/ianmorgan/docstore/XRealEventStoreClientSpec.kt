package ianmorgan.docstore

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import ianmorgan.docstore.dal.RealEventStore
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.xdescribe
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import java.util.*

/**
 * This can only be run with a real event store also running and
 * also seeded with the expected data (which is the demo set of events in
 * the event store)
 *
 * For now it can only be run manually, by renaming 'xdescribe' to 'describe'
 */

@RunWith(JUnitPlatform::class)
object XRealEventStoreClientSpec : Spek({

    val randoms = Random().ints(1, 1000000).iterator()

    xdescribe("the client to a real event store") {

        it("should read existing events for an aggregate") {
            val client = RealEventStore()
            val result = client.events("123")

            assert.that(result.size, equalTo(1))
            assert.that(result[0]["type"] as String, equalTo("AggregateEvent"))
        }


        it("should store a new event") {
            // store
            val aggregateId = randoms.nextInt().toString()
            val client = RealEventStore()
            val ev = mapOf(
                "id" to UUID.randomUUID().toString(),
                "type" to "AggregateEvent",
                "timestamp" to System.currentTimeMillis(),
                "creator" to "test",
                "aggregateId" to aggregateId
            )
            client.storeEvent(ev)

            // verify saved
            val result = client.events(aggregateId)
            assert.that(client.events(aggregateId)[0]["id"], equalTo(ev["id"]))
        }

        it("should read list of all aggregates ") {
            val client = RealEventStore()
            val result = client.aggregateKeys("Aggregate")
            assert.that(result.isEmpty(), equalTo(false))
        }

    }


})