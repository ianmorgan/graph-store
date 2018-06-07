package ianmorgan.docstore

import ianmorgan.docstore.graphql.GraphQLFactory2
import io.javalin.Javalin
import org.apache.commons.cli.Options
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import java.io.FileInputStream


fun main(args: Array<String>) {

    // Setup common command line options
    val options = Options()
    options.addOption("h", false, "display a help message")
    val parser = DefaultParser()
    val cmd = parser.parse(options, args)

    JavalinApp(7002, cmd).init()
}

class JavalinApp(private val port: Int, private val cmd : CommandLine) {
    lateinit var theDao : DocsDao

    fun init(): Javalin {

        if(cmd.hasOption("h")) {
            println ("help message")
        }


        val app = Javalin.create().apply {
            port(port)
            exception(Exception::class.java) { e, _ -> e.printStackTrace() }
            error(404) { ctx -> ctx.json("not found") }
        }.start()
        app.routes {



        }

        // setup the  main controller
        val starWarSchema = FileInputStream("src/schema/starwarsSimple.graphqls").bufferedReader().use { it.readText() }  // defaults to UTF-8
        val dao = DocsDao(starWarSchema)
        theDao = dao

        dao.daoForDoc("Droid").store(mapOf("id" to "2001", "name" to "R2D2"))
        val graphQL = GraphQLFactory2.build(starWarSchema,dao)

        val controller = Controller(dao, graphQL)
        controller.register(app)

        //JavalinJacksonPlugin.configure()

        return app

    }

    fun theDao() : DocsDao {
        return theDao
    }
}
