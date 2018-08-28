package ianmorgan.graphstore.graphql

import graphql.language.*
import graphql.schema.DataFetchingFieldSelectionSet
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLNonNull
import graphql.schema.GraphQLObjectType
import graphql.schema.idl.TypeDefinitionRegistry

/**
 * Helper to work alongside the GraphQLJava TypeDefinitionRegistry object model, mainly to provide
 * a richer & more type safe navigation of the object graph.
 */
class TypeDefinitionRegistryHelper constructor(registry: TypeDefinitionRegistry) {
    val tdr = registry

    /**
     * List of object definitions, excluding the "Query"
     */
    fun objectDefinitionNames(): List<String> {
        val result = ArrayList<String>()
        for (definition in tdr.getTypes(ObjectTypeDefinition::class.java)) {
            if (!(definition.name == "Query")) {
                result.add(definition.name)

            }
        }
        return result
    }

    fun scalarDefinitionNames(): List<String> {
        val result = ArrayList<String>()
        for (definition in tdr.getTypes(ScalarTypeDefinition::class.java)) {
            if (!(definition.name == "Query")) {
                result.add(definition.name)

            }
        }
        return result
    }

    /**
     * Return the ObjectTypeDefinition for this name or throw an exception
     */
    fun objectDefinition(name: String): ObjectTypeDefinition {
        return tdr.getType(name, ObjectTypeDefinition::class.java).get()
    }


    /**
     * List of 'InterfaceTypeDefinition' names
     */
    fun interfaceDefinitionNames(): List<String> {
        val result = ArrayList<String>()
        for (definition in tdr.getTypes(InterfaceTypeDefinition::class.java)) {
            result.add(definition.name)
        }
        return result
    }


    /**
     * List of 'InterfaceTypeDefinition'
     */
    fun unionDefinitionNames(): List<String> {
        val result = ArrayList<String>()
        for (definition in tdr.getTypes(UnionTypeDefinition::class.java)) {
            result.add(definition.name)
        }
        return result
    }

    /**
     * Return the InterfaceTypeDefinition for this name or throw an exception
     */
    fun interfaceDefinition(name: String): InterfaceTypeDefinition {
        return tdr.getType(name, InterfaceTypeDefinition::class.java).get()
    }

    /**
     * Return the UnionTypeDefinition for this name or throw an exception
     */
    fun unionDefinition(name: String): UnionTypeDefinition {
        return tdr.getType(name, UnionTypeDefinition::class.java).get()
    }


    /**
     * Return the ObjectTypeDefinition for the query node. There should always
     * be a query.
     */
    fun queryDefinition(): ObjectTypeDefinition {
        return tdr.getType("Query", ObjectTypeDefinition::class.java).get()
    }

    /**
     * Return the names of the ObjectTypes that implement this interface. So for
     * the standard starwars schema then 'Character' will return 'Droid' & 'Human'
     */
    fun objectsImplementingInterface(interfaceName: String): List<String> {
        val result = ArrayList<String>()
        for (definition in tdr.getTypes(ObjectTypeDefinition::class.java)) {
            for (implements in definition.implements) {

                val type = implements as TypeName
                if (type.name == interfaceName) {
                    result.add(definition.name)
                }
            }
        }
        return result
    }

    /**
     * Return the names of the ObjectTypes that this union can return
     */
    fun objectsInUnion(unionName: String): List<String> {
        val unionTypeDefinition = tdr.getType(unionName, UnionTypeDefinition::class.java).get()

        val result = ArrayList<String>()
        for (member in unionTypeDefinition.memberTypes) {
            val type = member as TypeName
            result.add(type.name)
        }
        return result
    }
}

class GraphQLFieldDefinitionHelper constructor(fieldDefinition: GraphQLFieldDefinition) {
    val definition = fieldDefinition

    fun foo(): String? {
        val t = definition.type
        if (t is GraphQLNonNull) {
            val w = t.wrappedType
            if (w is GraphQLObjectType) {

                return w.name
            }
        }
        return "??"
    }

}

class ObjectTypeDefinitionHelper constructor(typeDefinition: ObjectTypeDefinition, registry: TypeDefinitionRegistry?) {
    val otd = typeDefinition
    val registry = registry

    /**
     * Finds all fields that hold a list, regardless of whats in tbe list
     */
    fun listTypeFieldNames(): List<String> {
        val result = ArrayList<String>()
        for (field in otd.fieldDefinitions) {
            val rawType = field.type

            if (rawType is ListType) {
                result.add(field.name)
            }

            if (rawType is NonNullType) {
                val type = rawType.type
                if (type is ListType) {
                    result.add(field.name)
                }
            }
        }
        return result
    }

    /**
     * Finds all fields that return an object
     */
    fun objectTypeFieldNames(): List<String> {
        val result = ArrayList<String>()
        for (field in otd.fieldDefinitions) {
            val rawType = field.type

            if (rawType is TypeName) {
                if (isObject(field.name)) result.add(field.name)
            }

            if (rawType is NonNullType) {
                val type = rawType.type
                if (type is TypeName) {
                    if (isObject(field.name)) result.add(field.name)
                }
            }
        }
        return result
    }

    /**
//     * Finds all fields that return a scalar
//     */
//    fun scalarTypeFieldNames(): List<String> {
//        val result = ArrayList<String>()
//        for (field in otd.fieldDefinitions) {
//            val rawType = field.type
//
//            if (rawType is TypeName) {
//                if (isScalar(field.name)) result.add(field.name)
//            }
//
//            if (rawType is NonNullType) {
//                val type = rawType.type
//                if (type is TypeName) {
//                    if (isScalar(field.name)) result.add(field.name)
//                }
//            }
//        }
//        return result
//    }

    fun isInterface(fieldName: String): Boolean {
        val registryHelper = Helper.build(registry!!)
        val definition = fieldDefinition(fieldName)
            val rawType = definition.type

                if (rawType is NonNullType) {
                    val type = rawType.type
                    if (type is TypeName) {
                       return registryHelper.interfaceDefinitionNames().contains(type.name)
                    }
                    if (type is ListType) {
                        //return type.type
                    }
                }

                if (rawType is ListType) {
                    val type = rawType.type
                    if (type is TypeName) {
                        return registryHelper.interfaceDefinitionNames().contains(type.name)
                    }
                }

        return false
    }

    fun isObject(fieldName: String): Boolean {
        val registryHelper = Helper.build(registry!!)
        val definition = fieldDefinition(fieldName)

        val typeName = extractType(definition)
        return registryHelper.objectDefinitionNames().contains(typeName.name)
    }

    fun isScalar(fieldName: String): Boolean {
        val registryHelper = Helper.build(registry!!)
        val definition = fieldDefinition(fieldName)

        val typeName = extractType(definition)
        return registryHelper.scalarDefinitionNames().contains(typeName.name)

    }

    /**
     * Find the name of ID field, or null if there is no ID field
     */
    fun idFieldName(): String? {
        // navigate the schema information to find an ID field
        for (field in otd.fieldDefinitions) {
            val rawType = field.type
            if (rawType is NonNullType) {
                val type = rawType.type
                if (type is TypeName) {
                    if (type.name == "ID") {
                        return field.name
                    }
                }
            }
        }
        return null;
    }

    fun hasID(): Boolean {
        return idFieldName() != null
    }

    private fun fieldDefinition(fieldName: String): FieldDefinition {
        for (field in otd.fieldDefinitions) {
            if (field.name == fieldName) {
                return field
            }
        }
        throw RuntimeException("cannot find definition for $fieldName")
    }




    /**
     * Given a field name, go figure out its unpacked type name as
     * a String
     */
    fun typeForField(fieldName: String): String? {
        try {
            return extractType(fieldDefinition(fieldName)).name
        }
        catch (ex : RuntimeException){
            return null
        }
    }

    /**
     * Given a field name, go figure out its unpacked TypeName
     */
    private fun extractType(definition:  FieldDefinition) : TypeName {
        val type = definition.type

        if (type is TypeName){
            return type
        }
        else if (type is NonNullType) {
            val innerType = type.type
            if (innerType is TypeName) {
                return innerType
            }
            if (innerType is ListType) {
               return innerType.type as TypeName
            }
        }
        else if (type is ListType) {
            return type.type as TypeName
        }
        throw RuntimeException("Don't know what to do with $definition")
    }

    /**
     * Should this be treated as a linked document, i.e. does it hold
     * just an ID that should be expanded
     */
    fun isLinked(fieldName : String) : Boolean {
        if (isInterface(fieldName) || isObject(fieldName)){
            val typeName = typeForField(fieldName)
            return Helper.buildOTDH(registry!!,typeName!!).hasID()
        }
        return false
    }

}


class DataFetchingFieldSelectionSetHelper constructor(selectionSet: DataFetchingFieldSelectionSet) {
    val selectionSet = selectionSet

    fun argsForField(fieldName: String): Map<String, Any>? {
        for (set in selectionSet.arguments) {
            if (set.key == fieldName) {
                return set.value
            }
        }
        return null;
    }


}


object Helper {
    fun build(typeDefintion: TypeDefinitionRegistry): TypeDefinitionRegistryHelper {
        return TypeDefinitionRegistryHelper(typeDefintion)
    }

    fun buildOTDH(registry: TypeDefinitionRegistry, typeName: String): ObjectTypeDefinitionHelper {
        val otd = TypeDefinitionRegistryHelper(registry).objectDefinition(typeName)
        return build(otd,registry)
    }


    fun buildOTDH(registry: TypeDefinitionRegistry, definition: ObjectTypeDefinition): ObjectTypeDefinitionHelper {
        return ObjectTypeDefinitionHelper(definition,registry)

    }

    @Deprecated(message = "use buildOTDH variant instead")
    fun build(definition: ObjectTypeDefinition, registry: TypeDefinitionRegistry? = null): ObjectTypeDefinitionHelper {
        return ObjectTypeDefinitionHelper(definition,registry)
    }

    fun build(registry: TypeDefinitionRegistry, name: String): ObjectTypeDefinitionHelper {
        val definition = registry.getType(name, ObjectTypeDefinition::class.java).get()
        return ObjectTypeDefinitionHelper(definition,registry)
    }

    fun build(set: DataFetchingFieldSelectionSet): DataFetchingFieldSelectionSetHelper {
        return DataFetchingFieldSelectionSetHelper(set)
    }

    fun build(fieldDefintion: GraphQLFieldDefinition): GraphQLFieldDefinitionHelper {
        return GraphQLFieldDefinitionHelper(fieldDefintion)
    }
}