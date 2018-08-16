package ianmorgan.graphstore


import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.github.mustachejava.DefaultMustacheFactory
import ianmorgan.graphstore.controller.AdminController
import ianmorgan.graphstore.controller.Controller
import ianmorgan.graphstore.controller.SchemaController
import ianmorgan.graphstore.dal.*
import io.javalin.Javalin
import io.javalin.embeddedserver.Location
import io.javalin.translator.json.JavalinJacksonPlugin
import io.javalin.translator.template.JavalinMustachePlugin
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import java.io.File
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

class JavalinApp(private val port: Int, private val cmd: CommandLine) {
    lateinit var theDao: DocsDao

    fun init(): Javalin {

        var eventStoreClient: EventStoreClient = InMemoryEventStore()
        println("Starting...")
        if (cmd.hasOption("h")) {
            println("todo - add a help message")
            System.exit(0)
        }

        if (cmd.hasOption("E")) {
            println("Using a real event store")
            eventStoreClient = RealEventStore()
        }

        val mapper = ObjectMapper()
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        //mapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
        JavalinJacksonPlugin.configure(mapper)

        // Mustache template handling.
        val mf = DefaultMustacheFactory("views")
        JavalinMustachePlugin.configure(mf)

        val app = Javalin.create().apply {
            port(port)
            exception(Exception::class.java) { e, ctx ->
                // build the standard error response
                ctx.status(500)
                val payload = mapOf(
                    "message" to e.message,
                    "stackTrace" to e.stackTrace.joinToString("\n")
                )
                ctx.json(mapOf("errors" to listOf(payload)))
            }

            error(404) { ctx ->
                val payload = mapOf("message" to "not found")
                ctx.json(mapOf("errors" to listOf(payload)))
            }

            enableStaticFiles("/www", Location.CLASSPATH)
        }

        //  wireup external DAOS
        val externalDaoRegistry = ExternalDaoRegistry(eventStoreClient)
        registerStarshipDao(externalDaoRegistry)
        externalDaoRegistry.rebuildDaos()
        val externalDaos = externalDaoRegistry.allDaos()

        // wireup schema
        val starWarSchema =
            FileInputStream("src/schema/starwars_ex.graphqls").bufferedReader().use { it.readText() }  // defaults to UTF-8
        val dao = DocsDao(starWarSchema, eventStoreClient, externalDaos)
        theDao = dao

        // starting to wireup real state holder
        val stateHolder = StateHolder(eventStoreClient)
        stateHolder.build(starWarSchema, externalDaos)

        val dataLoader = DataLoader(stateHolder.docsDao)
        dataLoader.loadDirectory("src/test/resources/starwars_ex")

        Controller(stateHolder).register(app)
        SchemaController(stateHolder).register(app)
        AdminController(stateHolder).register(app)
        app.start()
        println("Ready :)")

        return app

    }

    private fun registerStarshipDao (registry: ExternalDaoRegistry)  {
        val mapper = """
                import ianmorgan.graphstore.mapper.MapperHelper;

                def helper = new MapperHelper(raw)
                helper.copyIfExists('name')
                helper.copyIfExists('manufacturer')
                helper.copyIfExists('model')
                helper.copyIfExists('length','lengthInMetres')
                helper.copyIfExists('cost_in_credits','costInCredits')
                return helper.output() """.trimIndent()

        val config = mapOf("baseUrl" to "https://swapi.co/api/starships/", "resultMapperScript" to mapper)


        registry.registerDao("Starship")
        registry.configureDao("Starship",config)
    }


}
