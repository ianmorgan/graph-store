package ianmorgan.graphstore

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import graphql.schema.idl.SchemaParser
import ianmorgan.graphstore.graphql.Helper
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import java.io.FileInputStream

/**
 * A collection of tests around the problems of embedded docs, i.e. types that
 * can only accessed as part of a large aggregate,  basically the case where a Type
 * defined in the schema DOESNOT have an ID field.
 *
 */
@RunWith(JUnitPlatform::class)
object HelperSpec : Spek({

    val starwarsSchema = FileInputStream("src/schema/starwars_ex.graphqls").bufferedReader().use { it.readText() }
    val registry = SchemaParser().parse(starwarsSchema)

    describe ("GraphQL queries over the embedded docs") {

//        beforeGroup {
//            val registry = SchemaParser().parse(starwarsSchema)
//            val human = Helper.buildOTDH(registry,"Human")
//
//
//
//        }

        it ("should help describe a Human ") {
            val human = Helper.buildOTDH(registry,"Human")

            // tests over collections
            assert.that(human.listTypeFieldNames(), equalTo(listOf("friends","appearsIn")))
            assert.that(human.isInterface("friends"), equalTo(true))
            assert.that(human.isObject("friends"), equalTo(false))
            //assert.that(human.isLinked("friends"), equalTo(true))
            assert.that(human.isInterface("appearsIn"), equalTo(false))
            assert.that(human.isObject("appearsIn"), equalTo(false))
            //assert.that(human.isLinked("appearsIn"), equalTo(false))

            // test over objects
            assert.that(human.objectTypeFieldNames(), equalTo(listOf("enemy")))
            assert.that(human.isObject("enemy"), equalTo(true))
            assert.that(human.isInterface("enemy"), equalTo(false))
            assert.that(human.isLinked("enemy"), equalTo(true))

            // tests over scalars
            //assert.that(human.scalarTypeFieldNames(), equalTo(listOf("enemy")))
        }

        it ("should help describe a Droid ") {
            val droid = Helper.buildOTDH(registry,"Droid")

            assert.that(droid.listTypeFieldNames(), equalTo(listOf("friends","appearsIn","starships")))
            assert.that(droid.isInterface("friends"), equalTo(true))
            assert.that(droid.isInterface("appearsIn"), equalTo(false))
            assert.that(droid.isInterface("starships"), equalTo(false))
            assert.that(droid.isObject("friends"), equalTo(false))
            assert.that(droid.isObject("appearsIn"), equalTo(false))
            assert.that(droid.isObject("starships"), equalTo(true))
        }

    }
})