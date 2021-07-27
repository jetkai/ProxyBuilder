package spb

import java.io.File

/**
 * @author Kai
 */
object Constants {

    var STAGE = "STARTING"

    const val THREADS = 1000 // TEST THREADS LIMIT

    const val IS_USING_PROXY = true
    const val EXIT_UPON_COMPLETION = true
    const val DEBUG_MODE = true

    val IS_PROXY_BUILDER_USER = System.getProperty("user.name").equals("proxybuilder")

}