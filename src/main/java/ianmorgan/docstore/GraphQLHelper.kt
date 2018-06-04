package ianmorgan.docstore

import graphql.language.InterfaceTypeDefinition
import graphql.language.ObjectTypeDefinition
import graphql.schema.idl.TypeDefinitionRegistry

/**
 * Helper to work alongside the GraphQLJava object model, mainly to provide
 * a richer more type safe navigation of object graph.
 */

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


    /**
     * List of 'InterfaceTypeDefinition'
     */
    fun interfaceDefinitionNames() : List<String> {
        val result = ArrayList<String>()
        for (definition in tdr.getTypes(InterfaceTypeDefinition::class.java)) {
                result.add(definition.name)

        }
        return result
    }

    /**
     * Return the InterfaceTypeDefinition for this name or throw an exception
     */
    fun interfaceDefinition (name : String) : InterfaceTypeDefinition {
        return tdr.getType(name,InterfaceTypeDefinition::class.java).get()
    }


    /**
     * Return the ObjectTypeDefinition for the query node. There should always
     * be a query.
     */
    fun queryDefinition() : ObjectTypeDefinition{
        return  tdr.getType("Query",ObjectTypeDefinition::class.java).get()
    }

}