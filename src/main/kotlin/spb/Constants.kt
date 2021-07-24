package spb

import java.io.File

/**
 * @author Kai
 */
object Constants {

    var STAGE = "STARTING"

    const val THREADS = 1000 // TEST THREADS LIMIT

    const val IS_USING_PROXY = true

    private const val SECRET_PATH = "C:\\Users\\Kai\\IntelliJProjects\\secrets"

    //RSPS IP TO TEST WITH
    val VICTIM_TEST_SERVER_IP = File("$SECRET_PATH\\VICTIM_TEST_SERVER_IP").readText() //EX - 127.0.0.1
    val VICTIM_TEST_SERVER_PORT = Integer.parseInt(File("$SECRET_PATH\\VICTIM_TEST_SERVER_PORT").readText()) //EX 43594

    val MY_SECRET_LOCAL_PATH = File("$SECRET_PATH\\MY_SECRET_LOCAL_PATH").readText()

    val PROXY_ENDPOINT_URL = File("$SECRET_PATH\\PROXY_ENDPOINT_URL").readText() //EX - https://your-proxy-api.com/proxies.json

}