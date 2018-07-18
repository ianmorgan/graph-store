package ianmorgan.docstore.dal

object DocReducer {


    fun reduceEvents (events :  List<Map<String, Any>>) : Map<String,Any>{
        // Simple reduce using inbuilt Kotlin methods for now
        // will not cover the more complicated scenarios

        var working : Map<String,Any?> = HashMap()

        for (event in events){
            val type = event["type"] as String


            if (type.endsWith("Updated")) {
                @Suppress("UNCHECKED_CAST")
                val payload = event["payload"] as Map<String,Any?>
                working = working
                    .plus(payload)      // merge event
                    .filterValues { it -> it != null }              // null indicates removal of the field
            }
            if (type.endsWith("Deleted")){
                working = HashMap()
            }
        }

        @Suppress("UNCHECKED_CAST")
        return working as Map<String,Any>
    }


}