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
        const val COMPARE_ARCHIVE_AND_JSON = 0
        const val COMPARE_ARCHIVE_WITH_JSON_CHECK = 1

        @JvmStatic
        fun main(args : Array<String>) {
            val connectTest = UniqueProxyChecker()
            connectTest.setConstants()
            Config.init()
            connectTest.init(COMPARE_ARCHIVE_WITH_JSON_CHECK)
        }
    }

    fun init(testType : Int): ProxyData {
        when (testType) {
            0 -> { //Compare Archive (GitHub) + Json (data/proxies/proxies.json)
                val proxies = loadProxiesFromTxt()
                val proxiesFromJson = loadProxiesFromJson()
                return uniquify(proxies, proxiesFromJson)
            }
            1 -> { //Compare Archive (GitHub) with (data/proxies/****-check.json)
                val workingProxies = loadProxiesFromTempTxt()
                val archive = loadArchiveFromGitHub()
                return uniquify(workingProxies, archive)
            }
        }
        return ProxyData(arrayOf(), arrayOf(), arrayOf(), arrayOf())
    }

    fun setConstants() {
        Constants.DEBUG_MODE = true
        Constants.DISPLAY_SUCCESS_CONNECTION_MESSAGE = true
        Constants.IS_PROXY_BUILDER_USER = true
    }

    private fun uniquify(proxies : ProxyData, proxiesArchive : ProxyData): ProxyData {

        proxies.socks4 = proxies.socks4.plus(proxiesArchive.socks4)
        proxies.socks5 = proxies.socks5.plus(proxiesArchive.socks5)
        proxies.http = proxies.http.plus(proxiesArchive.http)
        proxies.https = proxies.https.plus(proxiesArchive.https)

        println("START: SOCKS4[${proxies.socks4.size}], SOCKS5[${proxies.socks5.size}], " +
                "HTTP[${proxies.http.size}], HTTPS[${proxies.https.size}]")

        val filteredSocks4 = filterProxies(proxies.socks4).distinct().toMutableList()
        filteredSocks4.filter { proxiesArchive.socks4.contains(it) }.forEach { filteredSocks4.remove(it) }

        val filteredSocks5 = filterProxies(proxies.socks5).distinct().toMutableList()
        filteredSocks5.filter { proxiesArchive.socks5.contains(it) }.forEach { filteredSocks5.remove(it) }

        val filteredHttp = filterProxies(proxies.http).distinct().toMutableList()
        filteredHttp.filter { proxiesArchive.http.contains(it) }.forEach { filteredHttp.remove(it) }

        val filteredHttps = filterProxies(proxies.https).distinct().toMutableList()
        filteredHttps.filter { proxiesArchive.https.contains(it) }.forEach { filteredHttps.remove(it) }

        println("END: SOCKS4[${filteredSocks4.size}], SOCKS5[${filteredSocks5.size}]," +
                " HTTP[${filteredHttp.size}], HTTPS[${filteredHttps.size}]")

        proxies.socks4 = filteredSocks4.toTypedArray()
        proxies.socks5 = filteredSocks5.toTypedArray()
        proxies.http = filteredHttp.toTypedArray()
        proxies.https = filteredHttps.toTypedArray()

        val data = Json
        val finalJson = data.encodeToString(proxies)

        File("${Constants.PROXY_BUILDER_DATA_LOCATION}/data/proxies/proxies-unique.json").writeText(finalJson)
        return proxies
    }

    private fun filterProxies(proxyArray : Array<String>): Array<String> {
        val proxies = arrayListOf<String>()
        proxyArray.filter {
            it.contains(":") }.forEach {
            proxies += formatIp(it.replace("\n", "").replace("\r", "").replace(" ", ""))
        }
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

    private fun loadProxiesFromJson() : ProxyData {

        val proxyArchive = loadArchiveFromGitHub()

        val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

        val proxiesToMergeJson = File("${Constants.PROXY_BUILDER_DATA_LOCATION}/data/proxies/proxies-to-merge.json").readText()
        var proxiesToMerge = ProxyData(arrayOf(), arrayOf(), arrayOf(), arrayOf())
        try {
            proxiesToMerge = json.decodeFromString<Array<ProxyData>>("[$proxiesToMergeJson]").associateBy{ it }.keys.toMutableList()[0]
        } catch (i : Exception) { }

        val socks4 = proxiesToMerge.socks4.plus(proxyArchive.socks4).distinct().toTypedArray()
        val socks5 = proxiesToMerge.socks5.plus(proxyArchive.socks5).distinct().toTypedArray()
        val http = proxiesToMerge.http.plus(proxyArchive.http).distinct().toTypedArray()
        val https = proxiesToMerge.https.plus(proxyArchive.https).distinct().toTypedArray()

        return ProxyData(socks4, socks5, http, https)
    }

    private fun loadArchiveFromGitHub() : ProxyData {
        val proxyArchiveUrl = "https://github.com/jetkai/proxy-list/raw/main/archive/json/working-proxies-history.json"
        val proxyArchiveJson = try { URL(proxyArchiveUrl).readText() } catch (e : Exception) {
            println("Issue with connecting to githubusercontent.com:\n${e.message}") }

        val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

        var proxyArchive = ProxyData(arrayOf(), arrayOf(), arrayOf(), arrayOf())
        try {
            proxyArchive = json.decodeFromString<Array<ProxyData>>("[$proxyArchiveJson]").associateBy{ it }.keys.toMutableList()[0]
        } catch (i : Exception) { }
        return proxyArchive
    }

    public fun loadProxiesFromTxt(): ProxyData {
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

    private fun loadProxiesFromTempTxt() : ProxyData {
        val proxies = ProxyData(arrayOf(), arrayOf(), arrayOf(), arrayOf())

        val paths = arrayListOf(
            "${Constants.PROXY_BUILDER_DATA_LOCATION}/data/proxies/socks4-check.txt",
            "${Constants.PROXY_BUILDER_DATA_LOCATION}/data/proxies/socks5-check.txt",
            "${Constants.PROXY_BUILDER_DATA_LOCATION}/data/proxies/http-check.txt",
            "${Constants.PROXY_BUILDER_DATA_LOCATION}/data/proxies/https-check.txt"
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
                file.name.contains("socks4") -> socks4Proxies = file.readText().
                replace("[", "").replace("]", "").split(", ").toTypedArray()
                file.name.contains("socks5") -> socks5Proxies = file.readText().
                replace("[", "").replace("]", "").split(", ").toTypedArray()
                file.name.contains("https") -> httpsProxies = file.readText().
                replace("[", "").replace("]", "").split(", ").toTypedArray()
                file.name.contains("http") -> httpProxies = file.readText().
                replace("[", "").replace("]", "").split(", ").toTypedArray()
            }
        }

        proxies.socks4 += socks4Proxies
        proxies.socks5 += socks5Proxies
        proxies.http += httpProxies
        proxies.https += httpsProxies

        println(proxies.socks4.size)

        return proxies
    }
}