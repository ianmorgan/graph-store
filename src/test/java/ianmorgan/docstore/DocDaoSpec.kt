package ianmorgan.docstore

import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.throws
import graphql.language.ObjectTypeDefinition
import graphql.schema.idl.SchemaParser
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import kotlin.reflect.KClass

@RunWith(JUnitPlatform::class)
object DocDaoSpec : Spek({

    val schema = """
enum Episode {
  NEWHOPE
  EMPIRE
  JEDI
}

type Droid  {
  id: ID!
  name: String!
  friends: [Character]
  appearsIn: [Episode]!
  primaryFunction: String
}
"""

    lateinit var type: ObjectTypeDefinition

    describe("A DAO for a document") {
        beforeGroup {
            val schemaParser = SchemaParser()
            val typeDefinitionRegistry = schemaParser.parse(schema)
            type = typeDefinitionRegistry.getType("Droid", ObjectTypeDefinition::class.java).get()
        }

        it("should use the 'ID' field as the aggregate id") {
            val dao = DocDao(type)
            assert.that(dao.aggregateKey(), equalTo("id"))
        }

        it("should throw exception if there is no 'ID' field in schema") {
            val registry = SchemaParser().parse("type Droid { name: String!} ")
            val type = registry.getType("Droid", ObjectTypeDefinition::class.java).get()

            assert.that({ DocDao(type) }, throws<RuntimeException>())
        }

        it("should build the 'fields' collection from the GraphQL schema") {
            val dao = DocDao(type)
            assert.that(dao.fields().size, equalTo(5))
            assert.that(dao.fields().get("id"), equalTo(String::class as KClass<*>))
            assert.that(dao.fields().get("name"), equalTo(String::class as KClass<*>))
            assert.that(dao.fields().get("friends"), equalTo(List::class as KClass<*>))
            assert.that(dao.fields().get("appearsIn"), equalTo(List::class as KClass<*>))
            assert.that(dao.fields().get("primaryFunction"), equalTo(String::class as KClass<*>))
        }

        it("should store a valid doc") {
            val dao = DocDao(type)
            dao.store(mapOf("id" to "123", "name" to "Mouse Droid"))
            assert.that(1, equalTo(1))
        }

        it("should throw exception if there is no 'aggregateId' in the doc") {
            val dao = DocDao(type)
            assert.that({ dao.store(mapOf("name" to "Mouse Droid")) }, throws<RuntimeException>())
        }

        it("should throw exception if there is an unexpected field  in the doc") {
            val dao = DocDao(type)
            val doc = mapOf("id" to "123", "badlyNamedField" to "foo")

            fun messageMatcher(ex: RuntimeException) = ex.message.orEmpty().contains("Unexpected field badlyNamedField")
            assert.that({ dao.store(doc) }, throws(Matcher.invoke(::messageMatcher)));
        }

        it("should throw exception if field type doesn't match the schema") {
            val dao = DocDao(type)
            val doc = mapOf("id" to "123", "name" to 123)

            fun messageMatcher(ex: RuntimeException) = ex.message.orEmpty().contains("Types don't match for field name")
            assert.that({ dao.store(doc) }, throws(Matcher.invoke(::messageMatcher)))
        }


    }

})


