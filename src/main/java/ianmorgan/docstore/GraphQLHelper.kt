package ianmorgan.docstore

import graphql.language.ObjectTypeDefinition
import graphql.schema.idl.TypeDefinitionRegistry

class GraphQLHelper constructor(registry : TypeDefinitionRegistry){
    val tdr = registry

    /**
     * List of object definitions, excluding the "Query"
     */
    fun objectDefinitionNames() : List<String> {
        val result = ArrayList<String>()
        for (definition in tdr.getTypes(ObjectTypeDefinition::class.java)) {
            if (!(definition.name == "Query")) {
                result.add(definition.name)

            }
        }
        return result
    }

    /**
     * Return the ObjectTypeDefinition for this name or throw an exception
     */
    fun objectDefinition (name : String) : ObjectTypeDefinition {
        return tdr.getType(name,ObjectTypeDefinition::class.java).get()
    }

}