package spb

import spb.event.MonitorEvent
import spb.util.FileBuilder
import spb.util.SPBExecutorService

/**
 * @author Kai
 */
object Main {

    val SPBExecutorService = SPBExecutorService()

    @JvmStatic
    fun main(args : Array<String>) {
        this.init()
    }

    private fun init() {
        Constants.STAGE = "STARTING"
        SPBExecutorService.scheduleAtFixedRate(MonitorEvent())
    }

}