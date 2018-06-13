package ianmorgan.docstore.dal

object DocReducer {


    fun reduceEvents (events :  List<Map<String, Any?>>) : Map<String,Any>{
        // Simple reduce using inbuild Kotlin methods for now
        // will not cover the more complicated scenarios
        var working : Map<String,Any?> = HashMap()

        for (event in events){
            working = working
                .plus(event)                        // merge event
                .filterValues { it-> it != null }   // null indicates removal of the field
        }

        return working as Map<String,Any>
    }


}