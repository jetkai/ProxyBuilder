import kotlinx.serialization.ExperimentalSerializationApi
import spb.Constants
import spb.Main
import spb.net.proxy.ProxyGrabber
import spb.net.proxy.VerifiedProxies
import spb.util.Config
import spb.util.FileBuilder
import kotlin.system.exitProcess

@ExperimentalSerializationApi
class TestAllProxies {

    companion object {
        @JvmStatic
        fun main(args : Array<String>) {
            val testAllProxies = TestAllProxies()
            testAllProxies.setConstants()
            Config.init()
            testAllProxies.init()
        }
    }

    fun init() {
        while(true) {
            val command = readLine()
            if(command!!.isNotEmpty())
                executeCommand(command)
        }
    }

    private fun executeCommand(command : String) {
        when (command) {
            "go", "start" -> { ProxyGrabber().init() }
            "stop" -> {
                println("Stopping threads, waiting 10 seconds before build...")
                Main.SPBExecutorService.stop()
                Thread.sleep(10000)
                println("Building files...")
                build()
            }
            "exit", "quit" -> { exitProcess(0) }
        }
    }

    private fun build() {
        Main.SPBExecutorService.proxyBuilder.submit {
            when { Constants.DEBUG_MODE -> println("Attempting to build file...") }
            try {
                //Create files with verified proxies
                val socks4 = FileBuilder.sortByIp(VerifiedProxies.socks4)
                val socks5 = FileBuilder.sortByIp(VerifiedProxies.socks5)
                val http = FileBuilder.sortByIp(VerifiedProxies.http)
                val https = FileBuilder.sortByIp(VerifiedProxies.https)

                println("Verified Proxies: ${socks4.size}, ${socks5.size}, ${http.size}, ${https.size}")

                FileBuilder.buildTxtFiles(socks4, socks5, http, https, false)
                FileBuilder.buildJsonFiles(socks4, socks5, http, https, false)
                FileBuilder.buildCsvFile(socks4, socks5, http, https, false)
                FileBuilder.buildProxyArchive(socks4, socks5, http, https)
                //Update Readme file
                when { Constants.DEBUG_MODE -> println("Attempting to build Readme") }
                FileBuilder.buildReadmeFile()
                //Upload the files to GitHub using Git
             /*   when { Constants.DEBUG_MODE -> println("Attempting to execute GitActions") }
                GitActions().init()*/
            } catch (e: Exception) {
                when { Constants.DEBUG_MODE -> println("BUILDING ISSUE: \n${e.message} \n${e.stackTraceToString()}") }
            }
            if(Constants.EXIT_UPON_COMPLETION)
                exitProcess(0)
        }
    }

    private fun setConstants() {
        Constants.DEBUG_MODE = true
        Constants.DISPLAY_SUCCESS_CONNECTION_MESSAGE = true
        Constants.DISPLAY_FAILED_CONNECTION_MESSAGE = false
        Constants.IS_PROXY_BUILDER_USER = true
    }

}