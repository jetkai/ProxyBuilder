package spb.net.proxy

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import spb.Constants
import spb.Main
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
        val requestedData = URL(Constants.PROXY_ENDPOINT_URL).readText()

        val data = Json {
            this.prettyPrint = true
            this.encodeDefaults = true
            this.ignoreUnknownKeys = true
        }

        Constants.STAGE = "GRABBING_PROXIES"

        val proxies = data.decodeFromString<Array<ProxyInData>>("[$requestedData]").associateBy{ it }.keys.toMutableList()[0]

        for(proxy in filterProxies(proxies.socks4))
            validateProxy(proxy, "socks4")
        for(proxy in filterProxies(proxies.socks5))
            validateProxy(proxy, "socks5")
         for(proxy in filterProxies(proxies.http))
            validateProxy(proxy, "http")
        for(proxy in filterProxies(proxies.https))
            validateProxy(proxy, "https")

      //  validateProxy("173.212.220.96:3128", false)
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
        proxyTester.proxyPort = Integer.parseInt(splitProxy[1])
        proxyTester.type = type
        if(type == "socks4")
            proxyTester.socks4 = true

        Main.SPBExecutorService.schedule(proxyTester)
    }

}