package spb.net.proxy

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import spb.Constants
import spb.Main
import spb.util.Config
import spb.util.ProxiesTextToJson
import java.net.URL
import kotlin.system.exitProcess

/**
 * @author Kai
 */
@ExperimentalSerializationApi
class ProxyGrabber {

    fun init() {
       // FileBuilder.deleteOldProxyFiles()
        try {
            this.request()
        } catch (e : Exception) {
            println(e.stackTraceToString())
        }
    }

    private fun request() {
        val apiProxiesJson = try { URL(Config.values?.proxyEndpointUrl).readText() } catch (e : Exception) {
            println("Issue with connecting to proxyEndpointUrl from config.json:\n${e.stackTraceToString()}"); exitProcess(0) }
        val gitHubProxiesJson = try { URL(Config.values?.proxyEndpointGithubUrl).readText() } catch (e : Exception) {
            println("Issue with connecting to proxyEndpointGithubUrl from config.json:\n${e.stackTraceToString()}") }
        val localProxiesJson = try { ProxiesTextToJson().readText } catch (e : Exception) {
            println("Issue with converting text files to Json:\n${e.stackTraceToString()}") }

        var apiProxies = ProxyData(arrayOf(), arrayOf(), arrayOf(), arrayOf())
        var gitHubProxies = ProxyData(arrayOf(), arrayOf(), arrayOf(), arrayOf())
        var localProxies = ProxyData(arrayOf(), arrayOf(), arrayOf(), arrayOf())

        val data = Json {
            this.prettyPrint = true
            this.encodeDefaults = true
            this.ignoreUnknownKeys = true
        }

        Constants.STAGE = "GRABBING_PROXIES"

        try { apiProxies = data.decodeFromString<Array<ProxyData>>("[$apiProxiesJson]").associateBy{ it }.keys.toMutableList()[0] } catch (i : Exception) { }
        try { gitHubProxies = data.decodeFromString<Array<ProxyData>>("[$gitHubProxiesJson]").associateBy { it }.keys.toMutableList()[0] } catch (i : Exception) { }
        try { localProxies = data.decodeFromString<Array<ProxyData>>("[$localProxiesJson]").associateBy { it }.keys.toMutableList()[0] } catch (i : Exception) { }

        val finalSocks4 = apiProxies.socks4.plus(gitHubProxies.socks4).plus(localProxies.socks4)
        val finalSocks5 = apiProxies.socks5.plus(gitHubProxies.socks5).plus(localProxies.socks5)
        val finalHttp = apiProxies.http.plus(gitHubProxies.http).plus(localProxies.http)
        val finalHttps = apiProxies.https.plus(gitHubProxies.https).plus(localProxies.https)

        filterProxies(finalSocks4).forEach { proxy -> validateProxy(proxy, "socks4") }
        filterProxies(finalSocks5).forEach { proxy -> validateProxy(proxy, "socks5") }
        filterProxies(finalHttp).forEach { proxy -> validateProxy(proxy, "http") }
        filterProxies(finalHttps).forEach { proxy -> validateProxy(proxy, "https") }
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

    private fun validateProxy(proxy: String, type : String) {
        if(!proxy.contains(":")) {
            println("Failed to split : from proxy - $proxy, $type")
            return
        }
        val splitProxy = proxy.split(":")
        val proxyTester = ProxyTester()
        proxyTester.proxyAddress = splitProxy[0]
        proxyTester.proxyPort = splitProxy[1].toInt()
        proxyTester.type = type
        if(type == "socks4")
            proxyTester.socks4 = true

        Main.SPBExecutorService.schedule(proxyTester) //TODO CHANGE THIS LATER
    }
}
