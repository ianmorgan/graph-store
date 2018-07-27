package ianmorgan.docstore.dal

import ianmorgan.github.io.jsonUtils.JsonHelper
import org.json.JSONArray

/**
 * General face to an underlying event store holding the updates
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
    fun storeEvent(eventPayload: Map<String, Any>)

    /**
     * Retrieve all events for a given aggregate
     */
    fun events(aggregateId: String): List<Map<String, Any>>

    /**
     * Retrieve all events of given type(s)
     */
    fun eventsForType(types : Set<String>): List<Map<String, Any>>



    fun aggregateKeys(docType : String) : Set<String>

}

/**
 * A basic in memory store for testing & demo
 */
class InMemoryEventStore : EventStoreClient {

    private val repo: MutableMap<String, MutableList<Map<String, Any>>> = HashMap()
    private val allEvents : MutableList<Map<String, Any>> = ArrayList()


    override fun events(aggregateId: String): List<Map<String, Any>> {
        var eventsForDoc = repo[aggregateId]
        if (eventsForDoc == null) {
            return emptyList()
        } else {
            return eventsForDoc
        }
    }

    override fun eventsForType(types: Set<String>): List<Map<String, Any>> {
        val result = ArrayList<Map<String, Any>>()
        for (e in allEvents){
            val type = e["type"]!! as String
            if (type in types){
                result.add(e)
            }
        }
        return result
    }


    override fun storeEvent(eventPayload: Map<String, Any>) {
        val aggregateId = eventPayload["aggregateId"] as String
        var eventsForDoc = repo[aggregateId]
        if (eventsForDoc == null) {
            eventsForDoc = ArrayList()
            repo[aggregateId] = eventsForDoc
        }
        eventsForDoc.add(eventPayload)
        allEvents.add(eventPayload)
    }

    override fun aggregateKeys(docType : String): Set<String> {
        val result = HashSet<String>()
        for (entry in repo.entries){
          for (event in entry.value){
              if ((event["type"] as String).startsWith(docType) ){
                  result.add(entry.key)
                  break;
              }
          }
        }
        return result
    }

}


class RealEventStore : EventStoreClient {
    override fun eventsForType(types: Set<String>): List<Map<String, Any>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    val baseURL = "http://event-store:7001/"

    @Suppress("UNCHECKED_CAST")
    override fun events(aggregateId: String): List<Map<String, Any>> {
        // TODO - nicer handing of error conditions
        val response = khttp.get(baseURL + "events?aggregateId=$aggregateId")

        val result  = JsonHelper.jsonToMap(response.jsonObject)

        val payload = result["payload"] as Map<String,Any>;
        val events = payload["events"] as List<Map<String,Any>>
        return events
    }

    @Suppress("UNCHECKED_CAST")
    override fun aggregateKeys(docType: String): Set<String> {
        // TODO - nicer handing of error conditions
        val response = khttp.get(baseURL + "aggregates")

        val result  = JsonHelper.jsonToMap(response.jsonObject)

        val payload = result["payload"] as Map<String,Any>
        val aggragates = payload["aggregates"] as List<String>
        return aggragates.toSet()
    }

    override fun storeEvent(eventPayload: Map<String, Any>) {
        // TODO - nicer handing of error conditions
        val response = khttp.post(baseURL + "events", data = JSONArray(listOf(eventPayload)))
        if (response.statusCode != 200){
            throw RuntimeException("Problem saving event! ")
        }
    }

}