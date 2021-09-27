package spb.event

import kotlinx.serialization.ExperimentalSerializationApi
import spb.Constants
import spb.Main
import spb.git.GitActions
import spb.net.proxy.ProxyGrabber
import spb.net.proxy.VerifiedProxies
import spb.util.FileBuilder
import kotlin.system.exitProcess

/**
 * @author Kai
 */
@ExperimentalSerializationApi
class MonitorEvent : Event(88) { //Executes this Event every 88 minutes

    override fun run() {
        if(Constants.STAGE != "GRABBING_PROXIES" && !Constants.STAGE.contains("GIT"))
        //Grab proxies & test them
            ProxyGrabber().init()
        else if(Constants.STAGE == "GRABBING_PROXIES" && !Constants.STAGE.contains("GIT")) {
            //Interrupt all threads on SPBExecutorService->proxyThreadFactory
            Main.SPBExecutorService.stop()
            //Sleep MT 60s
            when { Constants.DEBUG_MODE -> println("Stopping threads") }
            Thread.sleep(60000)

            var buildAttempt = 1

            while (buildAttempt <= 3) {
                when { Constants.DEBUG_MODE -> println("Attempting to build file, attempt: $buildAttempt") }
                try {
                    //Create files with verified proxies
                    val socks4 = FileBuilder.sortByIp(VerifiedProxies.socks4)
                    val socks5 = FileBuilder.sortByIp(VerifiedProxies.socks5)
                    val http = FileBuilder.sortByIp(VerifiedProxies.http)
                    val https = FileBuilder.sortByIp(VerifiedProxies.https)
                    FileBuilder.buildTxtFiles(socks4, socks5, http, https, false)
                    FileBuilder.buildJsonFiles(socks4, socks5, http, https, false)
                    FileBuilder.buildCsvFile(socks4, socks5, http, https, false)
                    FileBuilder.buildProxyArchive(socks4, socks5, http, https)
                    //Update Readme file
                    when { Constants.DEBUG_MODE -> println("Attempting to build Readme") }
                    FileBuilder.buildReadmeFile()
                    //Upload the files to GitHub using Git
                    when { Constants.DEBUG_MODE -> println("Attempting to execute GitActions") }
                    GitActions().init()
                    break
                } catch (e: Exception) {
                    buildAttempt++
                    when { Constants.DEBUG_MODE -> println("BUILDING ISSUE: \n${e.message} \n${e.stackTraceToString()}") }
                    Thread.sleep(60000)
                }
            }
            if(Constants.EXIT_UPON_COMPLETION)
                exitProcess(0) //Exit if not leaving running
        }
    }

}