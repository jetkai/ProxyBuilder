package spb

import java.io.File

/**
 * @author Kai
 */
object Constants {

    var STAGE = "STARTING"

    const val THREADS = 1250 // TEST THREADS LIMIT

    const val IS_USING_PROXY = true
    const val EXIT_UPON_COMPLETION = true
    const val DEBUG_MODE = true

    val IS_WINDOWS = System.getProperty("os.name").startsWith("Windows")
    val IS_PROXY_BUILDER_USER = System.getProperty("user.name").equals("proxybuilder")

    private val IS_RUNNING_AS_JAR : Boolean = Main::class.java.getResource("Main.class")?.toString()!!.startsWith("jar:")
    val PROXY_BUILDER_DATA_LOCATION : String = if(IS_RUNNING_AS_JAR) File("../../").canonicalPath else File(".").canonicalPath

}