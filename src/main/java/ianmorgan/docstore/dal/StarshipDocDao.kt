package ianmorgan.docstore.dal

import org.json.JSONObject

/**
 * A custom DAO that retrieves starship data from an external REST API.
 * Due to the design of the API it needs to cache data for quick lookup
 *
 * A production quality implementation may need to consider cache expiry rules or
 * alternative solutions such as converting this to a set of events
 */
class StarshipDocDao : ReaderDao{
    val baseUrl = "https://swapi.co/api/"
    val lookup : MutableMap<String,Map<String,Any>> = HashMap()
    var initialised = false
    override fun retrieve(aggregateId: String): Map<String, Any> {

        if (!initialised){
            loadFromEndpoint()
        }
        if (lookup.containsKey(aggregateId)){
            return lookup[aggregateId]!!
        }
        else {
            // todo - something better here
            return mutableMapOf("message" to "not found")
        }
    }

    private fun loadFromEndpoint() {
        val response = khttp.get(url = baseUrl + "starships")
        if (response.statusCode == 200) {
            for (rawShipData in response.jsonObject.getJSONArray("results").toList()){
                if (rawShipData is Map<*,*>){

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

                    val name = rawShipData["name"]!! as String

                    val ship = HashMap<String,Any>() as MutableMap<String,Any>
                    ship["name"] = name
                    if (rawShipData.containsKey("model")) ship["model"] = rawShipData["model"]!!
                    if (rawShipData.containsKey("manufacturer")) ship["manufacturer"] = rawShipData["manufacturer"]!!
                    if (rawShipData.containsKey("length")) ship["lengthInMetres"] = (rawShipData["length"]!! as String)
                    if (rawShipData.containsKey("cost_in_credits")) ship["costInCredits"] = (rawShipData["cost_in_credits"]!! as String)

                    lookup[name] = ship

                }
            }
            initialised = true
        }

    }
}