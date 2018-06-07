package ianmorgan.docstore

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import graphql.GraphQL
import ianmorgan.docstore.graphql.GraphQLFactory2
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import java.io.FileInputStream

@RunWith(JUnitPlatform::class)
object GraphQLScalarTypesSpec : Spek({

    val allScalarTypes = FileInputStream("src/schema/allScalarTypes.graphqls").bufferedReader().use { it.readText() }
    lateinit var theDao: DocsDao
    lateinit var graphQL: GraphQL

    describe("Queries returning scalar types") {

        beforeGroup {
            // setup GraphQL & DAO with some initial data
            theDao = DocsDao(allScalarTypes)
            val allTypesDao = theDao.daoForDoc("AllTypes")
            allTypesDao.store(
                mapOf(
                    "id" to "everything",
                    "aString" to "A String",
                    "mandatoryString" to "Mandatory String",
                    "anInt" to 123,
                    "aFloat" to 99.9,
                    "aBoolean" to true
                )
            )

            val allNullVariants = theDao.daoForDoc("AllNullVariants")
            allNullVariants.store(
                mapOf(
                    "id" to "allvariants",
                    "storedAsNull" to null,
                    "storedAsEmpty" to ""
                ) as Map<String, Any>
            )

            graphQL = GraphQLFactory2.build(allScalarTypes, theDao)

        }

        it("should return all variants of null and empty") {

            val query = """{
                    allNullVariants(id: "allvariants") {
                       notStored,storedAsNull,storedAsEmpty
                    }}
"""
            val result = graphQL.execute(query)

            assert.that(result.errors.isEmpty(), equalTo(true))
            assert.that(
                result.getData<Any>().toString(),
                equalTo("{allNullVariants={notStored=null, storedAsNull=null, storedAsEmpty=}}")
            )
        }

        it("should return all values with the correct type") {

            val query = """{
                    allTypes(id: "everything") {
                       aString,mandatoryString,anInt,aFloat,aBoolean
                    }}
"""
            val result = graphQL.execute(query)

            assert.that(result.errors.isEmpty(), equalTo(true))
            assert.that(
                result.getData<Any>().toString(),
                equalTo("{allTypes={aString=A String, mandatoryString=Mandatory String, anInt=123, aFloat=99.9, aBoolean=true}}")
            )
        }
    }
})