package ianmorgan.docstore

/**
 * A Dao to query documents. The real implementation uses an event store,
 * but the client doesn't need to know this.
 */
class DocsDao {

    val droids = listOf (
        mapOf( "id" to "2001", "name" to "R2-D2",
            "appearsIn" to listOf("NEWHOPE","EMPIRE","JEDI") , "primaryFunction" to "Astromech"),
        mapOf( "id" to "2002", "name" to "C-3PO",
            "appearsIn" to listOf("NEWHOPE","EMPIRE","JEDI") , "primaryFunction" to "???")
    )

    val humans = listOf (
        mapOf( "id" to "1001", "name" to "Luke Skywalker",
            "appearsIn" to listOf("NEWHOPE","EMPIRE","JEDI")),
        mapOf( "id" to "1002", "name" to "Princess Leia",
            "appearsIn" to listOf("NEWHOPE","EMPIRE","JEDI"))
    )

    fun retrieve(aggregateId: String): Map<String, Any> {
        // todo - write some more elagant Kotlin code
        var ret = humans.find { it -> it["id"] == aggregateId }
        if (ret == null){
            ret = droids.find { it -> it["id"] == aggregateId }
        }
        if (ret != null ) return ret
        throw RuntimeException("nothing found !")
    }
}