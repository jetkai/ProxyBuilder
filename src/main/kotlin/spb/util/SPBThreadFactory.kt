package spb.util

import java.util.concurrent.ThreadFactory

/**
 * @author Kai
 */
class SPBThreadFactory (private val name : String) : ThreadFactory {

    private val threads: MutableList<Thread> = ArrayList()

    //Creates a new thread
    override fun newThread(r: Runnable?): Thread {
        val thread = Thread(r)
        threads.add(thread)
        thread.uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { _: Thread?, _: Throwable? ->
            println("[ALERT] ONE OF THE PROXY THREADS HAVE CRASHED")
        }
        return thread
    }

    //TODO - Using later
    fun interruptThread(thread : Thread) {
        thread.interrupt()
    }

    //TODO - After X amount of minutes, sometimes can be 100k+ new proxies, this can take forever
    fun interruptAllThreads() {
        threads.forEach { thread -> thread.interrupt() }
    }

}