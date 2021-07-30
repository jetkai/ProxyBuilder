package spb.net.proxy

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import spb.Constants
import spb.Main
import spb.util.Config
import spb.util.FileBuilder
import java.net.URL

/**
 * @author Kai
 */
class ProxyGrabber {

    fun init() {
        FileBuilder.deleteOldProxyFiles()
        this.request()
    }

    private fun request() {
        val gitHubData = URL(Config.values?.proxyEndpointGithubUrl).readText()
        val requestedData = URL(Config.values?.proxyEndpointUrl).readText()

        var gitHubProxies = ProxyInData(arrayOf(), arrayOf(), arrayOf(), arrayOf())
        var proxies = ProxyInData(arrayOf(), arrayOf(), arrayOf(), arrayOf())

        val data = Json {
            this.prettyPrint = true
            this.encodeDefaults = true
            this.ignoreUnknownKeys = true
        }

        Constants.STAGE = "GRABBING_PROXIES"

        try { gitHubProxies = data.decodeFromString<Array<ProxyInData>>("[$gitHubData]").associateBy { it }.keys.toMutableList()[0] } catch (i : Exception) { }
        try { proxies = data.decodeFromString<Array<ProxyInData>>("[$requestedData]").associateBy{ it }.keys.toMutableList()[0] } catch (i : Exception) { }

        filterProxies(proxies.socks4.plus(gitHubProxies.socks4)).forEach { proxy -> validateProxy(proxy, "socks4") }
        filterProxies(proxies.socks5.plus(gitHubProxies.socks5)).forEach { proxy -> validateProxy(proxy, "socks5") }
        filterProxies(proxies.http.plus(gitHubProxies.http)).forEach { proxy -> validateProxy(proxy, "http") }
        filterProxies(proxies.https.plus(gitHubProxies.https)).forEach { proxy -> validateProxy(proxy, "https") }
    }

    private fun filterProxies(proxyArray : Array<String>): Array<String> {
        val proxies = arrayListOf<String>()
        proxyArray.filter { it.contains(":") }.forEach { proxies += (it.replace("\n", "").replace("\r", "")) }
        return proxies.distinct().toTypedArray()
    }

    private fun validateProxy(proxy: String, type : String) {
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
