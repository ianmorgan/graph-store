package ianmorgan.docstore

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import graphql.GraphQL
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import java.io.FileInputStream

@RunWith(JUnitPlatform::class)
object GraphQLScalarTypesSpec : Spek({

    val allScalarTypes = FileInputStream("src/schema/allScalarTypes.graphqls").bufferedReader().use { it.readText() }
    lateinit var docsDao: DocsDao
    lateinit var graphQL : GraphQL

    describe ("Queries returning scalar types") {

//        id: ID!
//        aString: String
//        mandatoryString : String!
//        anInt, Int
//        aFloat Float

        beforeGroup {
            // setup GraphQL & DAO with some initial data
            // see https://github.com/graphql/graphql-js/blob/master/src/__tests__/starWarsData.js
            docsDao = DocsDao(allScalarTypes)
            val dao = docsDao.daoForDoc("AllTypes")
            dao.store(mapOf("id" to "everything",
                "aString" to "A String",
                "mandatoryString" to "Mandatory String",
                "anInt" to 123,
                "aFloat" to 99.9,
            "aBoolean" to true)     )


            graphQL = GraphQLFactory2.build(allScalarTypes,docsDao)

        }

        it ("should return all values with the correct type") {

            val query = """{
                    allTypes(id: "everything") {
                       id,aString,mandatoryString,anInt,aFloat,aBoolean
                    }}
"""
            val result = graphQL.execute(query)

            assert.that(result.errors.isEmpty(), equalTo(true))
            assert.that(result.getData<Any>().toString(),
                equalTo("{allTypes={id=everything, aString=A String, mandatoryString=Mandatory String, anInt=123, aFloat=99.9, aBoolean=true}}"))
        }


    }
})