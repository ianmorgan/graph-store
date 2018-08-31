package ianmorgan.graphstore.dal


/**
 * Holds a single find result, which is just a docType and associated Id
 */
data class FindResult(val docType: String, val id: String)

/**
 * Basic finder DAO, which can run a simple
 * search over a collection of fields
 */
interface FinderDao {

    /**
     *
     */
    fun findByFields(args : Map<String,Any> ): List<FindResult>

}