package ianmorgan.docstore.dal

import ianmorgan.github.io.jsonUtils.JsonHelper
import org.json.JSONArray

/**
 * General interface to an underlying event store holding the updates
 */
interface EventStoreClient {

    /**
     * Store the event for a document update. The event represents a single update to the
     * document.
     *
     * The use of pseudo elements to control the type of update is allowed a similar way to mongoDB
     * (https://docs.mongodb.com/manual/crud/#update-operations) but in a much more limited way
     *
     * TODO - expand on this in the docs
     *
     *
     */
    fun storeEvent(aggregateId: String, eventPayload: Map<String, Any>)

    /**
     * Retrieve all events for a given aggregate
     */
    fun events(aggregateId: String): List<Map<String, Any>>


    fun aggregateKeys() : Set<String>

}

/**
 * A basic in memory store for testing & demo
 */
class InMemoryEventStore : EventStoreClient {

    private val repo: MutableMap<String, MutableList<Map<String, Any>>> = HashMap()

    override fun events(aggregateId: String): List<Map<String, Any>> {
        var eventsForDoc = repo[aggregateId]
        if (eventsForDoc == null) {
            return emptyList()
        } else {
            return eventsForDoc
        }
    }

    override fun storeEvent(aggregateId: String, eventPayload: Map<String, Any>) {
        var eventsForDoc = repo[aggregateId]
        if (eventsForDoc == null) {
            eventsForDoc = ArrayList()
            repo[aggregateId] = eventsForDoc
        }
        eventsForDoc.add(eventPayload)
    }

    override fun aggregateKeys(): Set<String> {
        return repo.keys
    }


}

class RealEventStore : EventStoreClient {
    val baseURL = "http://event-store:7001/"
    override fun events(aggregateId: String): List<Map<String, Any>> {
        // TODO - nicer handing of error conditions
        val response = khttp.get(baseURL + "events?aggregateId=$aggregateId")

        val result  = JsonHelper.jsonToMap(response.jsonObject)
        println (result)

        val payload = result["payload"] as Map<String,Any>;
        val events = payload["events"] as List<Map<String,Any>>
        return events
    }

    override fun aggregateKeys(): Set<String> {
        // TODO - nicer handing of error conditions
        val response = khttp.get(baseURL + "aggregates")

        val result  = JsonHelper.jsonToMap(response.jsonObject)
        println (result)

        val payload = result["payload"] as Map<String,Any>;
        val aggragates = payload["aggregates"] as List<String>
        return aggragates.toSet()
    }

    override fun storeEvent(aggregateId: String, eventPayload: Map<String, Any>) {
        // TODO - nicer handing of error conditions
        val response = khttp.post(baseURL + "events", data = JSONArray(listOf(eventPayload)))
        if (response.statusCode != 200){
            throw RuntimeException("Problem saving event! ")
        }
    }

}