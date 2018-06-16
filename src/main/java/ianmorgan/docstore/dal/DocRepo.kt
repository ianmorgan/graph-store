package ianmorgan.docstore.dal

import ianmorgan.github.io.jsonUtils.JsonHelper

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
    override fun events(aggregateId: String): List<Map<String, Any>> {
        val response = khttp.get("http://event-store:7001/events?aggregateId=$aggregateId")

        val result  = JsonHelper.jsonToMap(response.jsonObject)
        println (result)

        val payload = result["payload"] as Map<String,Any>;
        val events = payload["events"] as List<Map<String,Any>>
        return events
    }

    override fun aggregateKeys(): Set<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun storeEvent(aggregateId: String, eventPayload: Map<String, Any>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}