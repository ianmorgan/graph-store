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
class ConfigurableRestDocDao : ReaderDao {
    val baseUrl = "https://swapi.co/api/"
    val lookup: MutableMap<String, Map<String, Any>> = HashMap()
    var initialised = false
    override fun retrieve(aggregateId: String): Map<String, Any> {

        val raw = loadFromEndpoint(aggregateId)


        val mapped = runMapper(raw)
        return mapped;
//        if (lookup.containsKey(aggregateId)) {
//            return lookup[aggregateId]!!
//        } else {
//            // todo - something better here
//            return mutableMapOf("message" to "not found")
//        }
    }

    private fun runMapper(rawData : Map<String,Any>) : Map<String,Any> {



//        val name = rawData["name"]!! as String
//
//        val ship = HashMap<String, Any>() as MutableMap<String, Any>
//        ship["name"] = name
//        if (rawShipData.containsKey("model")) ship["model"] = rawShipData["model"]!!
//        if (rawShipData.containsKey("manufacturer")) ship["manufacturer"] = rawShipData["manufacturer"]!!
//        if (rawShipData.containsKey("length")) ship["lengthInMetres"] = (rawShipData["length"]!! as String)
//        if (rawShipData.containsKey("cost_in_credits")) ship["costInCredits"] =
//                (rawShipData["cost_in_credits"]!! as String)
//
//        lookup[aggregateId] = ship

        val mapper = """
            import ianmorgan.docstore.mapper.MapperHelper;

            def helper = new MapperHelper(raw)
            helper.copyIfExists('name')
            helper.copyIfExists('manufacturer')
            helper.copyIfExists('model')
            helper.copyIfExists('length','lengthInMetres')
            helper.copyIfExists('cost_in_credits','costInCredits')
            return helper.output()

        """.trimIndent()


        val  binding = Binding()
        binding.setVariable("raw", rawData);
        val  shell =  GroovyShell(binding);
//
       val  value = shell.evaluate(mapper)
        println (value)

        return value as Map<String,Any>
    }

    private fun loadFromEndpoint(aggregateId: String) : Map<String,Any> {
        val response = khttp.get(url = baseUrl + "starships/" + aggregateId + "/")
        if (response.statusCode == 200) {
            val rawShipData = response.jsonObject.toMap()
            return rawShipData

//                    "name": "Executor",
//                    "model": "Executor-class star dreadnought",
//                    "manufacturer": "Kuat Drive Yards, Fondor Shipyards",
//                    "cost_in_credits": "1143350000",
//                    "length": "19000",
//                    "max_atmosphering_speed": "n/a",
//                    "crew": "279144",
//                    "passengers": "38000",
//                    "cargo_capacity": "250000000",
//                    "consumables": "6 years",
//                    "hyperdrive_rating": "2.0",
//                    "MGLT": "40",



        }
        throw RuntimeException("not found!")

    }
}