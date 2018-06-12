package ianmorgan.docstore

import graphql.language.InterfaceTypeDefinition

class InterfaceDao constructor(interfaceDefinition : InterfaceTypeDefinition, docDaoLookup :  Map<String, DocDao>) {
    val docDaoLookup =  docDaoLookup
    init {

    }

    fun retrieve(aggregateId: String): Map<String, Any>? {
        // todo - the list of daos to search should be retricted to just those
        // that implement the interface
        for (entry in docDaoLookup){
            val doc = entry.value.retrieve(aggregateId)
            if (doc != null){
                // todo - the set of fields should be restricted to just those in
                // the interface
                return doc
            }
        }
        return null
    }

}