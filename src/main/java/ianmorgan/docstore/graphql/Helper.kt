package ianmorgan.docstore.graphql

import graphql.language.*
import graphql.schema.idl.TypeDefinitionRegistry

/**
 * Helper to work alongside the GraphQLJava TypeDefinitionRegistry object model, mainly to provide
 * a richer & more type safe navigation of the object graph.
 */
class TypeDefinitionRegistryHelper constructor(registry : TypeDefinitionRegistry){
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
    fun queryDefinition() : ObjectTypeDefinition {
        return tdr.getType("Query", ObjectTypeDefinition::class.java).get()
    }
}

class ObjectTypeDefinitionHelper constructor(typeDefinition: ObjectTypeDefinition) {
    val otd = typeDefinition

    /**
     * Finds all the field name for non null types
     */
    fun listTypeFieldNames() : List<String> {
        val result = ArrayList<String>()
        for (field in otd.fieldDefinitions) {
            val rawType = field.type

            if (rawType is ListType) {
                result.add(field.name)
            }
        }
        return result
    }



    /**
     * Given a field name, go figure out its unpacked type
     */
    fun typeForField(fieldName : String) : String? {
        for (field in otd.fieldDefinitions) {
            val rawType = field.type
            if (field.name == fieldName) {

                if (rawType is NonNullType) {
                    val type = rawType.type
                    if (type is TypeName) {
                        return type.name
                    }
                    if (type is ListType) {
                        //return type.type
                    }
                }

                if (rawType is ListType) {
                    return (rawType.type as TypeName).name
                }
            }
            if (rawType is TypeName) {
                println(field.name)
                //working[field.name] = GraphQLMapper.graphQLTypeToJsonType(rawType.name)
            }

        }
        return null
    }



}

object Helper {
    fun build (tyepDefintion: TypeDefinitionRegistry) : TypeDefinitionRegistryHelper {
        return TypeDefinitionRegistryHelper(tyepDefintion)
    }

    fun build(definition : ObjectTypeDefinition) : ObjectTypeDefinitionHelper {
        return ObjectTypeDefinitionHelper(definition)
    }
}