import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import spb.Constants
import spb.net.proxy.ProxyData
import spb.util.Config
import java.io.File
import java.net.URL

@ExperimentalSerializationApi
class UniqueProxyChecker {

    companion object {
        @JvmStatic
        fun main(args : Array<String>) {
            val connectTest = UniqueProxyChecker()
            connectTest.setConstants()
            Config.init()
            connectTest.uniquify()
        }
    }

    fun setConstants() {
        Constants.DEBUG_MODE = true
        Constants.DISPLAY_CONNECTION_MESSAGE = true
        Constants.IS_PROXY_BUILDER_USER = true
    }

    fun uniquify() {

        val proxyArchiveUrl = "https://github.com/jetkai/proxy-list/raw/main/archive/json/working-proxies-history.json"
        val proxyArchiveJson = try { URL(proxyArchiveUrl).readText() } catch (e : Exception) {
            println("Issue with connecting to githubusercontent.com:\n${e.message}") }

        val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

        var proxyArchive = ProxyData(arrayOf(), arrayOf(), arrayOf(), arrayOf())
        try {
            proxyArchive = json.decodeFromString<Array<ProxyData>>("[$proxyArchiveJson]").associateBy{ it }.keys.toMutableList()[0]
        } catch (i : Exception) { }


        val proxiesToMergeJson = File("${Constants.PROXY_BUILDER_DATA_LOCATION}/data/proxies/proxies-to-merge.json").readText()
        var proxiesToMerge = ProxyData(arrayOf(), arrayOf(), arrayOf(), arrayOf())
        try {
            proxiesToMerge = json.decodeFromString<Array<ProxyData>>("[$proxiesToMergeJson]").associateBy{ it }.keys.toMutableList()[0]
        } catch (i : Exception) { }

        val socks4 = proxyArchive.socks4.distinct()
        val socks5 = proxyArchive.socks5.distinct()
        val http = proxyArchive.http.distinct()
        val https = proxyArchive.https.distinct()
        val proxies = loadProxiesFromTxt()

        proxies.socks4 = proxies.socks4.plus(proxiesToMerge.socks4)
        proxies.socks5 = proxies.socks5.plus(proxiesToMerge.socks5)
        proxies.http = proxies.http.plus(proxiesToMerge.http)
        proxies.https = proxies.https.plus(proxiesToMerge.https)

        println("START: SOCKS4[${proxies.socks4.size}], SOCKS5[${proxies.socks5.size}], " +
                "HTTP[${proxies.http.size}], HTTPS[${proxies.https.size}]")

        val filteredSocks4 = filterProxies(proxies.socks4).distinct().toMutableList()
        proxies.socks4.distinct().filter { socks4.contains(it) }.forEach { filteredSocks4.remove(it) }
        proxies.socks4 = filteredSocks4.toTypedArray()

        val filteredSocks5 = filterProxies(proxies.socks5).distinct().toMutableList()
        proxies.socks5.distinct().filter { socks5.contains(it) }.forEach { filteredSocks5.remove(it) }
        proxies.socks5 = filteredSocks5.toTypedArray()

        val filteredHttp = filterProxies(proxies.http).distinct().toMutableList()
        proxies.http.distinct().filter { http.contains(it) }.forEach { filteredHttp.remove(it) }
        proxies.http = filteredHttp.toTypedArray()

        val filteredHttps = filterProxies(proxies.https).distinct().toMutableList()
        proxies.https.distinct().filter { https.contains(it) }.forEach { filteredHttps.remove(it) }
        proxies.https = filteredHttps.toTypedArray()

        println("END: SOCKS4[${proxies.socks4.size}], SOCKS5[${proxies.socks5.size}]," +
                " HTTP[${proxies.http.size}], HTTPS[${proxies.https.size}]")

        val data = Json
        val finalJson = data.encodeToString(proxies)

        File("${Constants.PROXY_BUILDER_DATA_LOCATION}/data/proxies/proxies-unique.json").writeText(finalJson)
    }

    private fun filterProxies(proxyArray : Array<String>): Array<String> {
        val proxies = arrayListOf<String>()
        proxyArray.filter { it.contains(":") }.forEach { proxies += formatIp(it.replace("\n", "").replace("\r", "")) }
        return proxies.distinct().toTypedArray()
    }

    private fun formatIp(ip : String): String {
        var finalIp = ip
        val port = finalIp.split(":")[1]
        if (finalIp.contains(":"))
            finalIp = finalIp.split(":").toTypedArray()[0]
        val ipArray = finalIp.split(".").toTypedArray()
        for (i in 0..3) {
            if (ipArray[i].startsWith("0") && ipArray[i] != "0")
                ipArray[i] = ipArray[i].substring(1)
        }
        return ipArray.joinToString(separator = ".").plus(":$port")
    }


    private fun loadProxiesFromTxt(): ProxyData {
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
                file.name.contains("https") -> httpsProxies = file.readText().split(System.lineSeparator()).toTypedArray()
                file.name.contains("http") -> httpProxies = file.readText().split(System.lineSeparator()).toTypedArray()
            }
        }

        proxies.socks4 += socks4Proxies
        proxies.socks5 += socks5Proxies
        proxies.http += httpProxies
        proxies.https += httpsProxies
        return proxies
    }
}