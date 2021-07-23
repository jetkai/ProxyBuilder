package spb.util

import spb.Constants
import spb.event.Event
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * @author Kai
 */
class SPBExecutorService {

    private val mainExecutor = Executors.newScheduledThreadPool(Constants.THREADS, SPBThreadFactory("MainExecutor"))

    fun scheduleAtFixedRate(event : Event) {
        mainExecutor.scheduleAtFixedRate({
            if (event.isRunning)
                event.run()
        }, 0, event.delay.toLong(), TimeUnit.SECONDS)
    }

}