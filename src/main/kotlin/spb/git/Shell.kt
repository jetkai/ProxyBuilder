package spb.git

import spb.Constants
import spb.util.Config

/**
 * @author Kai
 */
class Shell { //TODO Change this, lot of test code that needs optimizing

    var CMD = when {
        Constants.IS_WINDOWS -> (arrayOf("cmd", "/c"/*, "start", "cmd", "/k"*/))
        else -> (arrayOf("/bin/sh", "-c")) //isLinux
    }

    var DIRECTORY = arrayOf("cd \"${Config.values?.proxyOutputPath}\" && ")

    fun executeShell(gitArguments : Array<String>) {
        if(Constants.IS_WINDOWS)
            println(Runtime.getRuntime().exec(CMD.plus(DIRECTORY).plus(gitArguments)).inputStream.reader().readText())
        else
            println(Runtime.getRuntime().exec(
                CMD.plus(DIRECTORY.joinToString { it }.replace("&&", "&") + gitArguments.joinToString(" ") { it }
                )).inputStream.reader().readText())
        Thread.sleep(10000)
    }

}