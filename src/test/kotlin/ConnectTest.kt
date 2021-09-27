import kotlinx.serialization.ExperimentalSerializationApi
import spb.Constants
import spb.net.proxy.ProxyTester
import spb.util.Config
import spb.util.FileBuilder
import java.util.concurrent.TimeUnit

@ExperimentalSerializationApi
fun main() {
    setConstants()
    Config.init()
    testConnect()
    //LocalProxyDatabase().load()
   // create()
   // testConnect()
    //println(formatIp("01.0.0.041"))
}

@ExperimentalSerializationApi
fun setConstants() {
    Constants.DEBUG_MODE = true
    Constants.DISPLAY_CONNECTION_MESSAGE = true
    Constants.IS_PROXY_BUILDER_USER = true
}

@ExperimentalSerializationApi
fun testConnect() {
    val proxyTester = ProxyTester()
    val socksProxy = arrayListOf<Any>("24.249.199.4", 4145, "socks5")
    val httpsProxy = arrayListOf<Any>("1.20.99.122", 8080, "socks5")
    proxyTester.proxyAddress = httpsProxy[0].toString()
    proxyTester.proxyPort = httpsProxy[1].toString().toInt()
    proxyTester.type = httpsProxy[2].toString()
    proxyTester.run()
}

private fun formatIp(ip : String): String {
    var finalIp = ip
    if (finalIp.contains(":"))
        finalIp = finalIp.split(":").toTypedArray()[0]
    val ipArray = finalIp.split(".").toTypedArray()
    for (i in 0..3) {
        if (ipArray[i].startsWith("0") && ipArray[i] != "0")
            ipArray[i] = ipArray[i].substring(1)
    }
    return ipArray.joinToString(separator = ".")
}

@ExperimentalSerializationApi
fun create() {
    val startTime = System.currentTimeMillis()
    FileBuilder.buildProxyArchive(arrayListOf("1.0.0.9", "1.0.3.1", "1.0.1.4", "1.0.0.0"),
        arrayListOf(), arrayListOf(), arrayListOf())
    val endTime = System.currentTimeMillis()
    val finalTime = endTime - startTime
    val formatTime = String.format("%d min, %d sec",
        TimeUnit.MILLISECONDS.toMinutes(finalTime),
        TimeUnit.MILLISECONDS.toSeconds(finalTime) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(finalTime))
    )
    println()
    println(formatTime)
}
