package ianmorgan.docstore.dal

import groovy.lang.Binding
import groovy.lang.GroovyShell

/**
 * A custom DAO that retrieves starship data from an external REST API.
 * Due to the design of the API it needs to cache data for quick lookup
 *
 * A production quality implementation may need to consider cache expiry rules or
 * alternative solutions such as converting this to a set of events
 */
class ConfigurableRestDocDao constructor(baseUrl : String, resultMapperScript : String? = null): ReaderDao {

    val aggregateKey = "id"
    val baseUrl = baseUrl
    val resultMapperScript = resultMapperScript
    val lookup: MutableMap<String, Map<String, Any>> = HashMap()
    var initialised = false
    override fun retrieve(aggregateId: String): Map<String, Any> {

        val raw = loadFromEndpoint(aggregateId)

        val mapped = runMapper(raw)
        return mapped;
    }


    private fun runMapper(rawData : Map<String,Any>) : Map<String,Any> {
        if (resultMapperScript != null){
            val  binding = Binding()
            binding.setVariable("raw", rawData);
            val  shell =  GroovyShell(binding);
            val  value = shell.evaluate(resultMapperScript)

            @Suppress("UNCHECKED_CAST")
            return value as Map<String,Any>
        }
        else {
            return rawData
        }
    }

    override fun aggregateKey(): String {
        return aggregateKey;
    }

    private fun loadFromEndpoint(aggregateId: String) : Map<String,Any> {
        val response = khttp.get(url = baseUrl + aggregateId )
        if (response.statusCode == 200) {
            val rawShipData = response.jsonObject.toMap()
            return rawShipData
        }
        throw RuntimeException("not found!")

    }
}