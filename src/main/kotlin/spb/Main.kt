package spb

import kotlinx.serialization.ExperimentalSerializationApi
import spb.event.MonitorEvent
import spb.util.Config
import spb.util.SPBExecutorService

/**
 * @author Kai
 */
@ExperimentalSerializationApi
object Main {

    val SPBExecutorService = SPBExecutorService() //TODO CHANGE THIS (TEMP)

    @JvmStatic
    fun main(args : Array<String>) {
        this.init()
    }

    private fun init() {
        Constants.STAGE = "STARTING"
        Config.init()
        SPBExecutorService.scheduleAtFixedRate(MonitorEvent()) //TODO CHANGE THIS (TEMP)
    }

}