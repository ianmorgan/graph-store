package ianmorgan.graphstore

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import ianmorgan.graphstore.dal.DocsDao
import ianmorgan.graphstore.dal.FindResult
import ianmorgan.graphstore.dal.FinderDao
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import java.io.FileInputStream

@RunWith(JUnitPlatform::class)
object DocFinderDaoSpec : Spek({

    val starWarSchema = FileInputStream("src/schema/starwars_ex.graphqls").bufferedReader().use { it.readText() }
    val docsDao = DocsDao(starWarSchema)
    val dataLoader = DataLoader(docsDao)
    dataLoader.loadDirectory("src/test/resources/starwars_ex")
    val dao = docsDao.daoForDoc("Human") as FinderDao


    describe("String matching scenarios") {

        it("should find by full name") {
            val result = dao.findByFields(mapOf("name" to "Luke Skywalker"))
            assert.that(result.map { it.id }, equalTo(listOf("1000")))
        }

        it("should find by full name case insensitive") {
            val result = dao.findByFields(mapOf("name" to "luke SKYWALKER"))
            assert.that(result.map { it.id }, equalTo(listOf("1000")))
        }

        it("should not find if name doesn't match") {
            val result = dao.findByFields(mapOf("name" to "missing"))
            assert.that(result, equalTo(emptyList()))
        }

        it("should find by partial name") {
            val result = dao.findByFields(mapOf("name_contains" to "luke"))
            assert.that(result.map { it.id }, equalTo(listOf("1000")))
        }
    }
    describe("list matching scenarios") {

        it("should find if contains expected list") {
            val result = dao.findByFields(mapOf("friends_contains" to listOf("1003", "2001")))
            assert.that(result.map { it.id} , equalTo(listOf("1002","1000")))
        }

        it("should find if contains expected value") {
            val result = dao.findByFields(mapOf("friends_contains" to "2001"))
            assert.that(result.map { it.id} , equalTo(listOf("1003","1002","1000")))
        }

    }


}) {
    fun assertIds(result: List<FindResult>, ids: List<String>) {
        assert.that(result.map { it.id }, equalTo(ids))
    }


}


