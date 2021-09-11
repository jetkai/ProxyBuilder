package spb.util

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import spb.Constants
import spb.net.proxy.ProxyData
import java.io.File

/**
 * @author Kai
 */
class ProxiesTextToJson {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            ProxiesTextToJson().convert()
        }
    }

    /**
     * Format:
     * 123.123.123.123:80
     * 123.123.123.124:4145
     */
    @OptIn(ExperimentalSerializationApi::class)
    public fun convert() : String {
        val proxies = ProxyData(arrayOf(), arrayOf(), arrayOf(), arrayOf())

        val paths = arrayListOf(
            "${Constants.PROXY_BUILDER_DATA_LOCATION}/data/proxies/socks4.txt",
            "${Constants.PROXY_BUILDER_DATA_LOCATION}/data/proxies/socks5.txt",
            "${Constants.PROXY_BUILDER_DATA_LOCATION}/data/proxies/http.txt",
            "${Constants.PROXY_BUILDER_DATA_LOCATION}/data/proxies/https.txt"
        )
        var socks4Proxies = arrayOf<String>()
        var socks5Proxies = arrayOf<String>()
        var httpProxies = arrayOf<String>()
        var httpsProxies = arrayOf<String>()

        for(path in paths) {
            val file = File(path)
            if(!file.exists())
                file.mkdir()
            when {
                file.name.contains("socks4") -> socks4Proxies = file.readText().split(System.lineSeparator()).toTypedArray()
                file.name.contains("socks5") -> socks5Proxies = file.readText().split(System.lineSeparator()).toTypedArray()
                file.name.contains("http") -> httpProxies = file.readText().split(System.lineSeparator()).toTypedArray()
                file.name.contains("https") -> httpsProxies = file.readText().split(System.lineSeparator()).toTypedArray()
            }
        }

        proxies.socks4 += socks4Proxies
        proxies.socks5 += socks5Proxies
        proxies.http += httpProxies
        proxies.https += httpsProxies

        val data = Json
        val finalJson = data.encodeToString(proxies)

        File("${Constants.PROXY_BUILDER_DATA_LOCATION}/data/proxies/proxies.json").writeText(finalJson) //Write Json if needed
        return finalJson
    }

}