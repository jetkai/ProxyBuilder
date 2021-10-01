package spb

import kotlinx.serialization.ExperimentalSerializationApi
import java.io.File

/**
 * @author Kai
 */
object Constants {

    var STAGE = "STARTING"

    const val THREADS = 1100 // TEST THREADS LIMIT
    const val CONNECTION_TIMEOUT = 3000

    const val IS_USING_PROXY = true
    const val EXIT_UPON_COMPLETION = true
    var DEBUG_MODE = true
    var DISPLAY_CONNECTION_MESSAGE = true

    val IS_WINDOWS = System.getProperty("os.name").startsWith("Windows")
    var IS_PROXY_BUILDER_USER = System.getProperty("user.name").equals("proxybuilder")

    @ExperimentalSerializationApi
    private val IS_RUNNING_AS_JAR : Boolean = Main::class.java.getResource("Main.class")?.toString()!!.startsWith("jar:")
    @ExperimentalSerializationApi
    val PROXY_BUILDER_DATA_LOCATION : String = if(IS_RUNNING_AS_JAR) File("../../").canonicalPath else File(".").canonicalPath

}