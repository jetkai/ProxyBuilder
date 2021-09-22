package spb.event

import kotlinx.serialization.ExperimentalSerializationApi
import spb.Constants
import spb.Main
import spb.git.GitActions
import spb.net.proxy.ProxyGrabber
import spb.net.proxy.VerifiedProxies
import spb.util.FileBuilder

/**
 * @author Kai
 */
@ExperimentalSerializationApi
class MonitorEvent : Event(90) { //Executes this Event every 90 minutes

    override fun run() {
        if(Constants.STAGE != "GRABBING_PROXIES" && !Constants.STAGE.contains("GIT"))
        //Grab proxies & test them
            ProxyGrabber().init()
        else if(Constants.STAGE == "GRABBING_PROXIES" && !Constants.STAGE.contains("GIT")) {
            //Interrupt all threads on SPBExecutorService->proxyThreadFactory
            Main.SPBExecutorService.proxyThreadFactory.interruptAllThreads()
            //Sleep MT 60s
            Thread.sleep(60000)
            try {
                //Create files with verified proxies
                val socks4 = FileBuilder.sortByIp(VerifiedProxies.socks4); val socks5 = FileBuilder.sortByIp(VerifiedProxies.socks5)
                val http = FileBuilder.sortByIp(VerifiedProxies.http); val https = FileBuilder.sortByIp(VerifiedProxies.https)
                FileBuilder.buildTxtFiles(socks4, socks5, http, https, false)
                FileBuilder.buildJsonFiles(socks4, socks5, http, https, false)
                FileBuilder.buildCsvFile(socks4, socks5, http, https, false)
                FileBuilder.buildProxyArchive()
                //Sleep MT 30s
                Thread.sleep(30000)
                //Update Readme file
                FileBuilder.updateReadme()
                //Sleep MT 30s
                Thread.sleep(30000)
                //Upload the files to GitHub using Git
                GitActions().init()
            } catch (e : Exception) {
                print(e.message)
            }
        }
    }

}