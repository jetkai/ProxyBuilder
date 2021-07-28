package spb.event

import spb.Constants
import spb.Main
import spb.git.GitActions
import spb.net.proxy.ProxyGrabber
import spb.util.FileBuilder

/**
 * @author Kai
 */
class MonitorEvent : Event(55) { //55 minutes test

    override fun run() {
        if(Constants.STAGE != "GRABBING_PROXIES" && !Constants.STAGE.contains("GIT"))
            ProxyGrabber().init()
        else if(Constants.STAGE == "GRABBING_PROXIES" && !Constants.STAGE.contains("GIT")) {
            Main.SPBExecutorService.proxyThreadFactory.interruptAllThreads()
            Thread.sleep(30000)
            FileBuilder.updateReadme()
            GitActions().init()
        }
    }

}