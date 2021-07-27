import spb.Constants
import spb.util.Config
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess

private var ADD = arrayOf("git", "add", ".")
private var COMMIT = arrayOf("git", "commit", "-m", "Updated-${SimpleDateFormat("dd/M/yyyy-hh:mm:ss").format(Date())}")
private var PUSH = arrayOf("git", "push", "origin", "main")
private var RELEASE = arrayOf("gh", "release", "create",
    SimpleDateFormat("yyMdd-hh").format(Date()), "--notes", "proxies-in-source-code.zip") //gh release create 0.0 --notes "test"


fun main() {
    Config.init()
    //println(System.getProperty("user.name"))
   //init()
}

fun init() {
    //Thread.sleep(60000)

    Constants.STAGE = "RUNNING GIT"
    //executeShell(ADD); println("Committing.")
   // executeShell(COMMIT); println("Pushing.")
   // executeShell(PUSH); println("Releasing.")
    executeShell(RELEASE); println("Done.")
    Constants.STAGE = "FINISHED"

    if(Constants.EXIT_UPON_COMPLETION)
        exitProcess(0) //Exit if not leaving running
}

var CMD = arrayOf("cmd", "/c", "start", "cmd", "/k")
var DIRECTORY = arrayOf("cd \"${Config.values?.proxyOutputPath}\" && ")

fun executeShell(gitArguments : Array<String>) {
    println(Runtime.getRuntime().exec(CMD.plus(DIRECTORY).plus(gitArguments)).inputStream.reader().readText())
    Thread.sleep(10000)
}

