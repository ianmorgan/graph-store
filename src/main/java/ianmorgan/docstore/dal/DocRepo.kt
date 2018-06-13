package ianmorgan.docstore.dal

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
    fun storeEvent(aggregateId: String, eventPayload: Map<String, Any?>)

    /**
     * Retrieve all events for a given aggregate
     */
    fun events(aggregateId: String): List<Map<String, Any?>>


    fun aggregateKeys() : Set<String>

}

/**
 * A basic in memory store for testing & demo
 */
class InMemoryEventStore : EventStoreClient {


    private val repo: MutableMap<String, MutableList<Map<String, Any?>>> = HashMap()

    override fun events(aggregateId: String): List<Map<String, Any?>> {
        var eventsForDoc = repo[aggregateId]
        if (eventsForDoc == null) {
            return emptyList()
        } else {
            return eventsForDoc
        }
    }

    override fun storeEvent(aggregateId: String, eventPayload: Map<String, Any?>) {
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