package spb

import spb.net.proxy.ProxyGrabber

/**
 * @author Kai
 */
object Main {

    @JvmStatic
    fun main(args : Array<String>) {
        ProxyGrabber().init()
       // ProxyTester().init()
    }

}