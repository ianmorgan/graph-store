package ianmorgan.docstore

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry
import ianmorgan.docstore.checker.ListChecker
import ianmorgan.docstore.checker.OneOf
import ianmorgan.docstore.checker.SchemaBuilder
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith

@RunWith(JUnitPlatform::class)
object SchemaBuilderSpec : Spek({

    val testSchema = """
         # A simple address structure
        type Address {
          street: String!
          suburb: String!
        }

        # The episodes in the Star Wars trilogy
        enum Episode {
            NEWHOPE
            EMPIRE
            JEDI
        }

        # A character from the Star Wars universe
        interface Character {
            id: ID!
            name: String!
            appearsIn: [Episode]!
        }

        type Human implements Character {
            id: ID!
            name: String!
            friends: [Character]
            appearsIn: [Episode]!
            homePlanet: String
            age: Int
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

    lateinit var registry : TypeDefinitionRegistry


    describe("Building a schema checker from graphQL") {

        beforeGroup {
            val schemaParser = SchemaParser()
            registry = schemaParser.parse(testSchema)
        }

        it("should build schema for 'Address' type") {
            val builder = SchemaBuilder(registry)

            val schema = builder.build("Address")
            val expected = mutableMapOf(
                "street" to OneOf(String::class.java),
                "suburb" to OneOf(String::class.java) )

            assert.that(schema, equalTo(expected as MutableMap<Any,Any>))
        }


        it("should build schema for 'Human' type") {
            val builder = SchemaBuilder(registry)

            val schema = builder.build("Human")
            val expected = mutableMapOf(
                "id" to OneOf(String::class.java),
                "name" to OneOf(String::class.java),
                "friends" to ListChecker(String::class.java),
                "appearsIn" to OneOf(ListChecker(String::class.java)),
                "homePlanet" to String::class.java,
                "age" to Long::class.javaObjectType)



            //                                            "friends": new OneOf(new ListSchemaChecker2(String.class))])

//            id: ID!
//            name: String!
//            friends: [Character]
//            appearsIn: [Episode]!
//            homePlanet: String
//            age: Integer


            assert.that(schema, equalTo(expected as MutableMap<Any,Any>))
        }



    }







})


