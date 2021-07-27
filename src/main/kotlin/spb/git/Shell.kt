package spb.git

import spb.Constants
import spb.util.Config

/**
 * @author Kai
 */
class Shell { //TODO Change this, lot of test code that needs optimizing

    var CMD = arrayOf("cmd", "/c"/*, "start", "cmd", "/k"*/)
    var DIRECTORY = arrayOf("cd \"${Config.values?.proxyOutputPath}\" && ")

    fun executeShell(gitArguments : Array<String>) {
        println(Runtime.getRuntime().exec(CMD.plus(DIRECTORY).plus(gitArguments)).inputStream.reader().readText())
        Thread.sleep(10000)
    }

}