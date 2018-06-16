package ianmorgan.docstore

import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.assertion.*
import ianmorgan.docstore.dal.DocsDao
import ianmorgan.docstore.dal.RealEventStore
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.xdescribe
import org.junit.Ignore
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import java.io.File

/**
 * This can only be run with a real event store also running and
 * also seeded with the expected data (which is the demo set of events in
 * the event store)
 *
 * For now it can only be run manually
 */

@RunWith(JUnitPlatform::class)
object RealEventStoreClientSpec : Spek({


    describe ("the client to a real event store") {

        it ("should read existing events for an aggregate ") {
            val client = RealEventStore()
            val result = client.events("123")

            assert.that(result.size, equalTo(1))
            assert.that(result[0]["type"] as String, equalTo("xAggregateEvent"))
        }
    }


})