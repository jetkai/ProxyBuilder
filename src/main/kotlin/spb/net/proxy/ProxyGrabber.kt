package spb.net.proxy

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import spb.Constants
import spb.util.SPBExecutorService
import java.net.URL

/**
 * @author Kai
 */
class ProxyGrabber {

    private val SPBExecutorService = SPBExecutorService()

    fun init() {
        this.request()
    }

    companion object {
        val verifiedSocks4 = arrayListOf<String>()
        val verifiedSocks5 = arrayListOf<String>()
    }
    //private val SBPExecutorService = SPBExecutorService()

    fun request() {
        val requestedData = URL(Constants.PROXY_ENDPOINT_URL).readText()

        val data = Json {
            this.prettyPrint = true
            this.encodeDefaults = true
            this.ignoreUnknownKeys = true
        }

        val dataArray = data.decodeFromString<Array<ProxyData>>("[$requestedData]")
        val dataMap = dataArray.associateBy { it }
        val proxies = dataMap.keys.toMutableList()[0]

        for(proxy in proxies.socks5) {
            validateProxy(proxy, false)
        }

     /*   for(proxy in proxies.socks4) {
            validateProxy(proxy, verifiedSocks4, false)
        }*/
    }

    private fun validateProxy(proxy: String, socks4: Boolean) {
        if(!proxy.contains(":"))
            return
        val splitProxy = proxy.split(":")

        val proxyTester = ProxyTester()
        proxyTester.proxyAddress = splitProxy[0]
        proxyTester.proxyPort = Integer.parseInt(splitProxy[1])
        if(socks4) proxyTester.socks4 = true
        SPBExecutorService.scheduleAtFixedRate(proxyTester)
    }

}