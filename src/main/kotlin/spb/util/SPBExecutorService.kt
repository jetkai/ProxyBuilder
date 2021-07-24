package spb.util

import spb.Constants
import spb.event.Event
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * @author Kai
 */
class SPBExecutorService {

    val proxyThreadFactory = SPBThreadFactory("ProxyExecutor")

    private val proxyExecutor = Executors.newFixedThreadPool(Constants.THREADS, proxyThreadFactory)
    private val monitorExecutor = Executors.newSingleThreadScheduledExecutor(SPBThreadFactory("MonitorExecutor"))

    fun schedule(event : Event) {
        proxyExecutor.submit {
            if (event.isRunning)
                event.run()
        }
    }

    fun scheduleAtFixedRate(event : Event) {
        monitorExecutor.scheduleAtFixedRate( {
            if (event.isRunning)
                event.run()
        }, 0, event.timeToExecute.toLong(), TimeUnit.MINUTES)
    }

}