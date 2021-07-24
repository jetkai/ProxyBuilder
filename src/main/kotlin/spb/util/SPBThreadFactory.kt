package spb.util

import java.util.concurrent.ThreadFactory

/**
 * @author Kai
 */
class SPBThreadFactory (private val name : String) : ThreadFactory {

    private val threads: MutableList<Thread> = ArrayList()

    //Creates a new thread
    override fun newThread(r: Runnable?): Thread {
        val t = Thread(r)
        threads.add(t)
        println("Total Threads:"+threads.size)
        t.uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { _: Thread?, _: Throwable? ->
            println(
                "[ALERT] ONE OF THE PROXY THREADS HAVE CRASHED"
            )
        }
        return t
    }

    //TODO - Using later
    fun interruptThread(thread : Thread) {
        thread.interrupt()
    }

    //TODO - After X amount of minutes, sometimes can be 100k+ new proxies, this can take forever
    fun interruptAllThreads() {
        for(thread in threads)
            thread.interrupt()
    }

}