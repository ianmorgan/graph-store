package docstore.ianmorgan.github.io

import io.javalin.Javalin
import org.apache.commons.cli.Options
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser



fun main(args: Array<String>) {

    // Setup common command line options
    val options = Options()
    options.addOption("h", false, "display a help message")
    val parser = DefaultParser()
    val cmd = parser.parse(options, args)

    JavalinApp(7001, cmd).init()
}

class JavalinApp(private val port: Int, private val cmd : CommandLine) {

    fun init(): Javalin {

        if(cmd.hasOption("h")) {
            println ("help message")
        }


        val app = Javalin.create().apply {
            port(port)
            exception(Exception::class.java) { e, ctx -> e.printStackTrace() }
            error(404) { ctx -> ctx.json("not found") }
        }.start()

        app.routes {



        }

        // setup the  main controller
        //val eventDao = EventDao()
        //eventDao.load("src/test/resources/examples")

        val controller = Controller()
        controller.register(app)

        //JavalinJacksonPlugin.configure()

        return app

    }
}
