package spb.git

import spb.Constants

/**
 * @author Kai
 */
class Shell { //TODO Change this, lot of test code that needs optimizing

    var CMD = arrayOf("cmd", "/c"/*, "start", "cmd", "/k"*/)
    var DIRECTORY = arrayOf("cd \"${Constants.MY_SECRET_LOCAL_PATH}\" && ")

    fun executeShell(gitArguments : Array<String>) {
        println(Runtime.getRuntime().exec(CMD.plus(DIRECTORY).plus(gitArguments)).inputStream.reader().readText())
        Thread.sleep(10000)
    }

}