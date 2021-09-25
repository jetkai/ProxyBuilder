package spb.git

import kotlinx.serialization.ExperimentalSerializationApi
import spb.Constants
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess

/**
 * @author Kai
 */
@ExperimentalSerializationApi
class GitActions { //TODO Change this, lot of test code that needs optimizing

    private var add = arrayOf("git", "add", ".")
    private var commit = arrayOf("git", "commit", "-m", "Updated-${SimpleDateFormat("dd/MM/yyyy-HH:mm:ss").format(Date())}")
    private var push = arrayOf("git", "push", "origin", "main")
    private var release = arrayOf("gh", "release", "create",
        SimpleDateFormat("yyMMdd-HH").format(Date()), "--notes", "proxies-in-source-code.zip")

    fun init() {
        Thread.sleep(30000)

        Constants.STAGE = "RUNNING GIT"

        Shell().executeShell(add)
        when { Constants.DEBUG_MODE -> println("Committing.") }

        Shell().executeShell(commit)
        when { Constants.DEBUG_MODE -> println("Pushing.") }

        Shell().executeShell(push)
        when { Constants.DEBUG_MODE -> println("Releasing.") }

        Shell().executeShell(release)
        when { Constants.DEBUG_MODE -> println("Done.") }

        Constants.STAGE = "FINISHED"

        if(Constants.EXIT_UPON_COMPLETION)
            exitProcess(0) //Exit if not leaving running
    }

}