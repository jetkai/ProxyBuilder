package spb.git

/**
 * @author Kai
 */
class Shell { //TODO Change this, lot of test code that needs optimizing

    var CMD = arrayOf("cmd", "/c"/*, "start", "cmd", "/k"*/)
    var DIRECTORY = arrayOf("cd \"C:\\Users\\Kai\\IntelliJProjects\\free-socks-proxies-autoupdate\" && ")

    fun executeShell(gitArguments : Array<String>) {
        println(Runtime.getRuntime().exec(CMD.plus(DIRECTORY).plus(gitArguments)).inputStream.reader().readText())
        Thread.sleep(10000)
    }

}