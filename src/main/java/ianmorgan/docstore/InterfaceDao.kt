package ianmorgan.docstore


import graphql.language.InterfaceTypeDefinition
import graphql.language.ListType
import graphql.language.NonNullType
import graphql.language.TypeName
import graphql.schema.idl.TypeDefinitionRegistry
import ianmorgan.docstore.graphql.Helper
import ianmorgan.docstore.graphql.TypeDefinitionRegistryHelper

/**
 * A Dao that knows how to retrieve an interface by delegating to the DocDao for each
 * doc that implements the interface
 */
class InterfaceDao constructor(
    interfaceName: String,
    typeDefinitionRegistry: TypeDefinitionRegistry,
    docDaoLookup: Map<String, DocDao>
) {
    private val docDaoLookup = docDaoLookup
    //val tdr = typeDefinitionRegistry
    //val interfaceName = interfaceName
    private val implementingDocs = HashSet<String>()
    private val fields = HashSet<String>()

    init {
        val helper = Helper.build(typeDefinitionRegistry)
        val interfaceDefinition = helper.interfaceDefinition(interfaceName)

        initDaoList(helper, interfaceName)
        initFieldList(interfaceDefinition)
    }


    fun retrieve(aggregateId: String): Map<String, Any>? {
        for (docName in implementingDocs) {
            val doc = docDaoLookup.get(docName)!!.retrieve(aggregateId)
            if (doc != null) {
                val copy = HashMap<String, Any>()
                for (entry in doc.entries) {
                    if (fields.contains(entry.key)) {
                        copy.put(entry.key, entry.value)
                    }
                }
                return copy
            }
        }
        return null
    }

    private fun initDaoList(helper: TypeDefinitionRegistryHelper, interfaceName: String) {
        for (docName in helper.objectDefinitionNames()) {
            val objectDefinition = helper.objectDefinition(docName)
            for (type in objectDefinition.getImplements()) {
                if (type is TypeName) {
                    if (type.name == interfaceName) {
                        implementingDocs.add(docName)
                    }
                }
            }
        }
    }

    private fun initFieldList(typeDefinition: InterfaceTypeDefinition) {
        for (field in typeDefinition.fieldDefinitions) {
            val rawType = field.type
            if (rawType is NonNullType) {
                val type = rawType.type
                if (type is TypeName) {
                    fields.add(field.name)
                }
                if (type is ListType) {
                    fields.add(field.name)
                }
            } else {
                if (rawType is TypeName) {
                    fields.add(field.name)
                }
                if (rawType is ListType) {
                    fields.add(field.name)
                }
            }
        }
    }

}