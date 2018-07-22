package ianmorgan.docstore

import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.throws
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry
import ianmorgan.docstore.checker.ValidatorMode
import ianmorgan.docstore.dal.DocDao
import ianmorgan.docstore.dal.MapHolder
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import kotlin.reflect.KClass

@RunWith(JUnitPlatform::class)
object DocDaoSpec : Spek({

    val starWarsSchema = """
        enum Episode {
          NEWHOPE
          EMPIRE
          JEDI
        }

        interface Character {
          id: ID!
          name: String!
        }

        type Droid  {
          id: ID!
          name: String!
          friends: [Character]
          appearsIn: [Episode]!
          primaryFunction: String
        }
""".trimIndent()

    val beatleSchema = """
        # Some skills
        enum Skill {
          SINGER
          DRUMMER
          COMPOSER
          GUITARIST
        }


        # A simple address structure
        type Address {
          street: String!
          suburb: String!
        }


        # A member of the beatles
        type Beatle  {
          id: ID!
          name: String!
          skills : [Skill]
          address : Address
        }
    """.trimIndent()

    lateinit var typeDefinitionRegistry : TypeDefinitionRegistry

    describe("A DAO for a document") {
        beforeGroup {
            val schemaParser = SchemaParser()
            typeDefinitionRegistry = schemaParser.parse(starWarsSchema)
        }

        it("should use the 'ID' field as the aggregate id") {
            val dao = DocDao(typeDefinitionRegistry  ,"Droid")
            assert.that(dao.aggregateKey(), equalTo("id"))
        }

        it("should throw exception if there is no 'ID' field in schema") {
            val registry = SchemaParser().parse("type Droid { name: String!} ")
            assert.that({ DocDao(registry,"Droid") }, throws<RuntimeException>())
        }

        it("should build the 'fields' collection from the GraphQL schema") {
            val dao = DocDao(typeDefinitionRegistry,"Droid")
            assert.that(dao.fields().size, equalTo(5))
            assert.that(dao.fields().get("id"), equalTo(String::class as KClass<*>))
            assert.that(dao.fields().get("name"), equalTo(String::class as KClass<*>))
            assert.that(dao.fields().get("friends"), equalTo(List::class as KClass<*>))
            assert.that(dao.fields().get("appearsIn"), equalTo(List::class as KClass<*>))
            assert.that(dao.fields().get("primaryFunction"), equalTo(String::class as KClass<*>))
        }

        it("should store a valid doc") {
            val dao = DocDao(typeDefinitionRegistry,"Droid")
            val doc = mapOf("id" to "123", "name" to "Mouse Droid", "appearsIn" to listOf("EMPIRE"))
            dao.store(doc)
            val stored = dao.retrieve("123")
            assert.that(doc, equalTo(stored))
        }

        it("should throw exception if there is no 'aggregateId' in the doc") {
            val dao = DocDao(typeDefinitionRegistry,"Droid")
            assert.that({ dao.store(mapOf("name" to "Mouse Droid")) }, throws<RuntimeException>())
        }

        it("should throw exception if there is an unexpected field  in the doc") {
            val dao = DocDao(typeDefinitionRegistry,"Droid")
            val doc = mapOf("id" to "123", "badlyNamedField" to "foo")

            fun messageMatcher(ex: RuntimeException) = ex.message.orEmpty().contains("badlyNamedField is not in the schema")
            assert.that({ dao.store(doc) }, throws(Matcher.invoke(::messageMatcher)));
        }

        it("should throw exception if field type doesn't match the schema") {
            val dao = DocDao(typeDefinitionRegistry,"Droid")
            val doc = mapOf("id" to "123", "name" to 123)

            fun messageMatcher(ex: RuntimeException) = ex.message.orEmpty().contains("name : 123 is not a String")
            assert.that({ dao.store(doc) }, throws(Matcher.invoke(::messageMatcher)))
        }

        it("should query on document field") {
            val dao = DocDao(typeDefinitionRegistry,"Droid")
            dao.store(mapOf("id" to "101", "name" to "Mouse Droid"), ValidatorMode.Skip)
            dao.store(mapOf("id" to "102", "name" to "BB-8"), ValidatorMode.Skip)
            dao.store(mapOf("id" to "103", "name" to "Interrogation Droid"), ValidatorMode.Skip)

            val stored = dao.findByField(fieldNameExpression = "name", value =  "Mouse Droid")
            assert.that(listOf(mapOf("id" to "101", "name" to "Mouse Droid")), equalTo(stored))
        }

        it("should query using contains wildcard") {
            val dao = DocDao(typeDefinitionRegistry,"Droid")
            val mouse = mapOf("id" to "101", "name" to "Mouse Droid") as Map<String,Any>
            val bb8 = mapOf("id" to "102", "name" to "BB-8") as Map<String,Any>
            val interrogation = mapOf("id" to "103", "name" to "Interrogation Droid") as Map<String,Any>
            dao.store(mouse, ValidatorMode.Skip)
            dao.store(bb8,  ValidatorMode.Skip)
            dao.store(interrogation,  ValidatorMode.Skip)

            assert.that(dao.findByField(fieldNameExpression = "name_contains", value =  "Droid"), equalTo(listOf(mouse,interrogation)))
            assert.that(dao.findByField(fieldNameExpression = "name_contains", value =  "droid"), equalTo(listOf(mouse,interrogation)))
            assert.that(dao.findByField(fieldNameExpression = "name_contains", value =  "vader"), equalTo(listOf()))
        }



    }

    describe("A DAO for documents with embedding") {
        beforeGroup {
            val schemaParser = SchemaParser()
            typeDefinitionRegistry = schemaParser.parse(beatleSchema)
        }

        it("should use the 'ID' field as the aggregate id") {
            val dao = DocDao(typeDefinitionRegistry  ,"Beatle")
            assert.that(dao.aggregateKey(), equalTo("id"))
        }

        it("should include the embedded Address") {
            val dao = DocDao(typeDefinitionRegistry  ,"Beatle")

            assert.that(dao.fields().size, equalTo(4))
            assert.that(dao.fields().get("id"), equalTo(String::class as KClass<*>))
            assert.that(dao.fields().get("name"), equalTo(String::class as KClass<*>))
            assert.that(dao.fields().get("skills"), equalTo(List::class as KClass<*>))
            assert.that(dao.fields().get("address"), equalTo(MapHolder::class as KClass<*>))

            //val a = dao.fields().get("address") as Map<String,Any>
            //println(a)
//            assert.that(dao.fields().get("primaryFunction"), equalTo(String::class as KClass<*>))
        }





    }


})


