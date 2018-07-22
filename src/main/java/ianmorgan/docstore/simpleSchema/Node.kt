package ianmorgan.docstore.simpleSchema

/**
 * A simpler representation of the underlying GraphQL schema model which
 * is sufficient for basic mapping and validation of data
 */

import kotlin.reflect.KClass

// everything is a Node

sealed class Node

data class BaseNode (val name : String, val mandatory : Boolean) : Node()

/**
 * Contains a scalar value and its Java type
 */
data class ScalarNode (val name : String, val mandatory : Boolean, val type : KClass<Any>) : Node()

/**
 * Contains a List of scalar values
 */
data class ScalarListNode (val name : String, val mandatory : Boolean, val type : KClass<Any>) : Node()

/**
 * An Object, which is basically a list of nodes
 */
data class ObjectNode (val name : String, val mandatory : Boolean, val items : List<Node>) : Node()

/**
 * A list of Objects
 */
data class ObjectListNode (val name : String, val mandatory : Boolean, val type : ObjectNode) : Node()


