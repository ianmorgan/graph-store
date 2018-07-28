package ianmorgan.graphstore.dal

import java.util.*
import kotlin.collections.HashMap

class ExternalDaoRegistry(eventStore: EventStoreClient = InMemoryEventStore()){
    private val es = eventStore
    private val daos = HashMap<String,ReaderDao>()

    /**
     * Register a new DAO against a doc. Normally it will need configuring afterwards
     */
    fun registerDao (docType : String){
        es.storeEvent(buildRegisterEvent(docType, "ConfigurableRestDocDao"))
    }

    /**
     * Add/update configuration for a DAO. Each DAO must accept a
     * a key/value collection with its supported configuration options
     */
    fun configureDao(docType : String, configuration:  Map<String, Any>  ){
        es.storeEvent(buildConfigEvent(docType,configuration))
    }

    /**
     * reduce all events to rebuild the correct DAO state. This is slightly
     * brutal in that all DAO will be rebuilt.
     */
    fun rebuildDaos(){
        // all events grouped by DAO
        val eventsByDao =
            es.eventsForType(setOf("ExternalDaoRegistered","ExternalDaoConfigured"))
                .groupBy( {ev -> ev["aggregateId"] as String})

        // build each DAO in turn
        daos.clear()
        for ((docType,events) in eventsByDao){
            var config : Map<String,Any?> = HashMap()

            for (event in events){
                val type = event["type"] as String

                if (type == "ExternalDaoConfigured") {
                    @Suppress("UNCHECKED_CAST")
                    val payload = event["payload"] as Map<String,Any?>
                    config = config
                        .plus(payload)      // merge event
                        .filterValues { it -> it != null }              // null indicates removal of the field
                }

            }
            daos[docType] = ConfigurableRestDocDao( configuration = config as Map<String,Any>)

        }
    }

    fun daoForDoc(docType : String) : ReaderDao {
        return daos[docType]!!
    }

    fun allDaos() : HashMap<String,ReaderDao>{
        return daos
    }

    private fun buildRegisterEvent(docType: String, implementingClass : String): Map<String, Any> {
        val ev = HashMap<String, Any>()
        ev["type"] = "ExternalDaoRegistered"
        ev["id"] = UUID.randomUUID().toString()
        ev["aggregateId"] = docType
        ev["timestamp"] = System.currentTimeMillis()
        ev["creator"] = "graph-store"
        ev["payload"] = mapOf("implementingClass" to implementingClass)
        return ev
    }

    private fun buildConfigEvent(docType: String, configuration:  Map<String, Any>): Map<String, Any> {
        val ev = HashMap<String, Any>()
        ev["type"] = "ExternalDaoConfigured"
        ev["id"] = UUID.randomUUID().toString()
        ev["aggregateId"] = docType
        ev["timestamp"] = System.currentTimeMillis()
        ev["creator"] = "graph-store"
        ev["payload"] = configuration
        return ev
    }

}