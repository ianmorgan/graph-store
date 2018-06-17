package ianmorgan.docstore

import ianmorgan.docstore.dal.DocsDao
import ianmorgan.docstore.dal.EventStoreClient
import ianmorgan.docstore.dal.InMemoryEventStore
import ianmorgan.docstore.dal.RealEventStore
import ianmorgan.docstore.graphql.GraphQLFactory2
import io.javalin.Javalin
import org.apache.commons.cli.Options
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import java.io.FileInputStream


fun main(args: Array<String>) {

    // Setup common command line options
    val options = Options()

        //options.addOption( "a", "all", false, "do not hide entries starting with ." );
    options.addOption("h", "help", false, "display a help message")
    options.addOption("E", "eventstore", false, "use a real event store")

    val parser = DefaultParser()
    val cmd = parser.parse(options, args)

    JavalinApp(7002, cmd).init()
}

class JavalinApp(private val port: Int, private val cmd : CommandLine) {
    lateinit var theDao : DocsDao

    fun init(): Javalin {
        var eventStoreClient : EventStoreClient = InMemoryEventStore()
        println ("Starting...")
        if(cmd.hasOption("h")) {
            println ("help message")
        }

        if (cmd.hasOption("E")) {
            println("use a real event store")
            eventStoreClient = RealEventStore()
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
        val dao = DocsDao(starWarSchema,eventStoreClient)
        theDao = dao

        val dataLoader = DataLoader(dao)
        dataLoader.loadDirectory("src/test/resources/starwars")

        val graphQL = GraphQLFactory2.build(starWarSchema,dao)

        val controller = Controller(dao, graphQL)
        controller.register(app)

        println ("Ready :)")


        //JavalinJacksonPlugin.configure()

        return app

    }

    fun theDao() : DocsDao {
        return theDao
    }
}
