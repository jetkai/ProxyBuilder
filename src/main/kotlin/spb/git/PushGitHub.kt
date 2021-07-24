package spb.git

import spb.Constants
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author Kai
 */
class PushGitHub {

    private var ADD = arrayOf("git", "add", ".")
    private var COMMIT = arrayOf("git", "commit", "-m", "Proxies-Updated-${SimpleDateFormat("dd/M/yyyy-hh:mm:ss").format(Date())}")
    private var PUSH = arrayOf("git", "push", "origin", "main")

    fun init() {
        Thread.sleep(60000)
        Constants.STAGE = "RUNNING GIT"
        println("Adding.")
        Shell().executeShell(ADD)
        println("Committing.")
        Shell().executeShell(COMMIT)
        println("Pushing.")
        Shell().executeShell(PUSH)
        println("Done.")
        Constants.STAGE = "FINISHED"
    }

}