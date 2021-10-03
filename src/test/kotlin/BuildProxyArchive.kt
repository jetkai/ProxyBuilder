import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonArray
import spb.Constants
import spb.net.proxy.ProxyData
import spb.util.Config
import spb.util.FileBuilder
import java.io.File
import java.net.URL

@ExperimentalSerializationApi
class BuildProxyArchive {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Config.init()
            BuildProxyArchive().init()
        }
    }

    fun init() {
        setConstants()
        checkArchive()
    }

    private fun checkArchive() {

        val freshProxies = UniqueProxyChecker().init(1)

        val socks4Array = freshProxies.socks4
        val socks5Array = freshProxies.socks5
        val httpArray = freshProxies.http
        val httpsArray = freshProxies.https

        val proxyArchiveUrl = "https://github.com/jetkai/proxy-list/raw/main/archive/json/working-proxies-history.json"
        val proxyArchiveJson = try { URL(proxyArchiveUrl).readText() } catch (e : Exception) {
            println("Issue with connecting to githubusercontent.com:\n${e.message}") }

        val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

        var proxyArchive = ProxyData(arrayOf(), arrayOf(), arrayOf(), arrayOf())
        try {
            proxyArchive = json.decodeFromString<Array<ProxyData>>("[$proxyArchiveJson]").associateBy{ it }.keys.toMutableList()[0]
        } catch (i : Exception) { }

        println("(Compare Archive) START: SOCKS4[${proxyArchive.socks4.size}], SOCKS5[${proxyArchive.socks5.size}], " +
                "HTTP[${proxyArchive.http.size}], HTTPS[${proxyArchive.https.size}]")

        val combinedSocks4 = proxyArchive.socks4.plus(socks4Array).distinct()
        val combinedSocks5 = proxyArchive.socks5.plus(socks5Array).distinct()
        val combinedHttp = proxyArchive.http.plus(httpArray).distinct()
        val combinedHttps = proxyArchive.https.plus(httpsArray).distinct()

        val socks4 = FileBuilder.sortByIp(combinedSocks4)
        val socks5 = FileBuilder.sortByIp(combinedSocks5)
        val http = FileBuilder.sortByIp(combinedHttp)
        val https = FileBuilder.sortByIp(combinedHttps)

        println("(Compare Archive) START: SOCKS4[${socks4.size}], SOCKS5[${socks5.size}], " +
                "HTTP[${http.size}], HTTPS[${https.size}]")

        val prettyJson = Json { prettyPrint = true; encodeDefaults = true }

        val allProxies = buildJsonObject {
            putJsonArray("socks4") { for (proxy in socks4) add(proxy) }
            putJsonArray("socks5") { for (proxy in socks5) add(proxy) }
            putJsonArray("http") { for (proxy in http) add(proxy) }
            putJsonArray("https") { for (proxy in https) add(proxy) }
        }

        File("${Config.values?.proxyOutputPath}/archive/json/working-proxies-history-beautify2.json").writeText(prettyJson.encodeToString(allProxies))

    }

    private fun setConstants() {
        Constants.DEBUG_MODE = true
        Constants.DISPLAY_SUCCESS_CONNECTION_MESSAGE = true
        Constants.DISPLAY_FAILED_CONNECTION_MESSAGE = false
        Constants.IS_PROXY_BUILDER_USER = true
    }

}