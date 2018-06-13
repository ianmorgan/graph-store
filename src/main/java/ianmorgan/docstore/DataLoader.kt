package ianmorgan.docstore

import ianmorgan.docstore.dal.DocsDao
import org.json.JSONObject
import java.io.File

class DataLoader constructor(docsDao : DocsDao) {
    val docsDao = docsDao

    /**
     * Loads the data files in the directory directly via the DAO
     */
    fun loadDirectory(directory : String){

        File(directory).walk().forEach {
            if (it.name.endsWith(".json")) {
                println ("loading $it")
                val content = it.readText()
                val json = JSONObject(content)

                // find the matching dao
                val docType = json.getString("docType")
                val dao = docsDao.daoForDoc(docType)

                // data to load
                val data = json.toMap()
                data.remove("docType")

                dao.store(data)
            }
        }

    }

}