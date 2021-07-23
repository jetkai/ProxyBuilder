package spb

import spb.net.proxy.ProxyGrabber
import spb.net.proxy.ProxyTester

object Main {

    @JvmStatic
    fun main(args : Array<String>) {
        ProxyGrabber().init()
       // ProxyTester().init()
    }

}