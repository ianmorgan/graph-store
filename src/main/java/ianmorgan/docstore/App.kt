package ianmorgan.docstore


import com.fasterxml.jackson.databind.ObjectMapper
import ianmorgan.docstore.dal.*
import io.javalin.Javalin
import io.javalin.embeddedserver.Location
import io.javalin.translator.json.JavalinJacksonPlugin
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
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
        //.writerWithDefaultPrettyPrinter()
        //mapper.s(DefaultPrettyPrinter())
        JavalinJacksonPlugin.configure(mapper)


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

        // setup the  main controller
        val externalDaos = HashMap<String,ReaderDao>()
        externalDaos.put("Starship", starshipDao())
        val starWarSchema =
            FileInputStream("src/schema/starwars_ex.graphqls").bufferedReader().use { it.readText() }  // defaults to UTF-8
        val dao = DocsDao(starWarSchema, eventStoreClient, externalDaos)
        theDao = dao

        // starting to wireup real state holder
        val stateHolder = StateHolder(eventStoreClient)
        stateHolder.build(starWarSchema, externalDaos)

        val dataLoader = DataLoader(stateHolder.docsDao)
        dataLoader.loadDirectory("src/test/resources/starwars_ex")

        //val graphQL = GraphQLFactory.build(starWarSchema, dao)


        Controller(stateHolder).register(app)
        SchemaController(stateHolder).register(app)
        app.start()
        println("Ready :)")


        //JavalinJacksonPlugin.configure()

        return app

    }

    private fun starshipDao () : ConfigurableRestDocDao {
        val mapper = """
                import ianmorgan.docstore.mapper.MapperHelper;

                def helper = new MapperHelper(raw)
                helper.copyIfExists('name')
                helper.copyIfExists('manufacturer')
                helper.copyIfExists('model')
                helper.copyIfExists('length','lengthInMetres')
                helper.copyIfExists('cost_in_credits','costInCredits')
                return helper.output() """.trimIndent()

        val dao = ConfigurableRestDocDao(baseUrl = "https://swapi.co/api/starships/", resultMapperScript = mapper)
        return dao;
    }


}
