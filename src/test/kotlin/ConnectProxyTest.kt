import kotlinx.serialization.ExperimentalSerializationApi
import spb.Constants
import spb.net.proxy.ProxyTester
import spb.util.Config

@ExperimentalSerializationApi
class ConnectProxyTest {

    companion object {
        @JvmStatic
        fun main(args : Array<String>) {
            val connectTest = ConnectProxyTest()
            connectTest.setConstants()
            Config.init()
            connectTest.testConnect()
        }
    }

    fun setConstants() {
        Constants.DEBUG_MODE = true
        Constants.DISPLAY_CONNECTION_MESSAGE = true
        Constants.IS_PROXY_BUILDER_USER = true
    }

    fun testConnect() {
        val proxyTester = ProxyTester()
        val socksProxy = arrayListOf<Any>("24.249.199.4", 4145, "socks5")
        val httpsProxy = arrayListOf<Any>("1.20.99.122", 8080, "socks5")
        proxyTester.proxyAddress = socksProxy[0].toString()
        proxyTester.proxyPort = socksProxy[1].toString().toInt()
        proxyTester.type = socksProxy[2].toString()
        proxyTester.run()
    }
}