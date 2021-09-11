package spb.git

import spb.Constants
import spb.util.Config

/**
 * @author Kai
 */
class Shell { //TODO Change this, lot of test code that needs optimizing

    private var command = when {
        Constants.IS_WINDOWS -> (arrayOf("cmd", "/c"/*, "start", "cmd", "/k"*/))
        else -> (arrayOf("/bin/sh", "-c")) //isLinux
    }

    private var directory = arrayOf("cd \"${Config.values?.proxyOutputPath}\" && ")

    fun executeShell(gitArguments : Array<String>) {
        if(Constants.IS_WINDOWS)
            println(Runtime.getRuntime().exec(command.plus(directory).plus(gitArguments)).inputStream.reader().readText())
        else
            println(Runtime.getRuntime().exec(
                command.plus(directory.joinToString { it } + gitArguments.joinToString(" ") { it }
                )).inputStream.reader().readText())
        Thread.sleep(60000)
    }

}