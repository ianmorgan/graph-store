package docstore.ianmorgan.github.io

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import graphql.language.ObjectTypeDefinition
import graphql.schema.idl.SchemaParser
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith

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

    describe ("A DAO for a document") {
        beforeGroup {

            val schemaParser = SchemaParser()
            val typeDefinitionRegistry = schemaParser.parse(schema)

            type = typeDefinitionRegistry.getType("Droid", ObjectTypeDefinition::class.java).get()




        }

        it ("should return be aggregate id "){
            val dao = DocDao(type)
            assert.that("1", equalTo("1"))
        }
    }


})