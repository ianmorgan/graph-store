package docstore.ianmorgan.github.io

/**
 * A Dao to query documents. The real implementation uses an event store,
 * but the client doesn't need to know this.
 */
class DocDao {

    fun retrieve(aggregateId: String): Map<String, Any> {
        return mapOf("name" to "homer", "hairColour" to "bald", "mainCharacter" to true)
    }
}