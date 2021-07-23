package spb.util

import java.util.concurrent.ThreadFactory

/**
 * @author Kai
 */
class SPBThreadFactory (private val name : String) : ThreadFactory {

    private val threads: MutableList<Thread> = ArrayList()

    override fun newThread(r: Runnable?): Thread {
        val t = Thread(r)
        threads.add(t)
        t.uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { _: Thread?, _: Throwable? ->
            println(
                "[ALERT] ONE OF THE PROXY THREADS HAVE CRASHED"
            )
        }
        return t
    }

    fun interruptThread(thread : Thread) {
        thread.interrupt()
    }

    fun getThreads(): List<Thread?> {
        return threads
    }

}