package spb.git

import spb.Constants
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess

/**
 * @author Kai
 */
class GitActions { //TODO Change this, lot of test code that needs optimizing

    private var ADD = arrayOf("git", "add", ".")
    private var COMMIT = arrayOf("git", "commit", "-m", "Updated-${SimpleDateFormat("dd/M/yyyy-hh:mm:ss").format(Date())}")
    private var PUSH = arrayOf("git", "push", "origin", "main")
    private var RELEASE = arrayOf("gh", "release", "create",
        SimpleDateFormat("yyMdd-hh").format(Date()), "--notes", "proxies-in-source-code.zip")

    fun init() {
        Thread.sleep(60000)

        Constants.STAGE = "RUNNING GIT"
        Shell().executeShell(ADD); println("Committing.")
        Shell().executeShell(COMMIT); println("Pushing.")
        Shell().executeShell(PUSH); println("Releasing.")
        Shell().executeShell(RELEASE); println("Done.")
        Constants.STAGE = "FINISHED"

        if(Constants.EXIT_UPON_COMPLETION)
            exitProcess(0) //Exit if not leaving running
    }

}