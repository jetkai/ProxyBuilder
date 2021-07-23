package spb.net.proxy

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import spb.Constants
import java.net.URL

/**
 * @author Kai
 */
class ProxyGrabber {

    fun init() {
        this.request()
    }

    fun request() {
        val requestedData = URL(Constants.PROXY_ENDPOINT_URL).readText()

        val data = Json {
            this.prettyPrint = true
            this.encodeDefaults = true
            this.ignoreUnknownKeys = true
        }

        val dataArray = data.decodeFromString<Array<ProxyData>>("[$requestedData]")
        val dataMap = dataArray.associateBy { it }
        val finalDataMap = dataMap.keys.toMutableList()[0]

        println(dataArray.size)
    }

}