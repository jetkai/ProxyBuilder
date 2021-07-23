package spb.util

import java.io.File

class FileBuilder {

    private val test = arrayListOf("123.123.123.123:8080", "444.111.444.111:0992", "127.0.0.1:80")

    companion object {
    @JvmStatic
        fun main(args:Array<String>) {
            FileBuilder().buildTxt()
        }
    }

    fun buildTxt() {
        File("proxies.txt").writeText(test.joinToString("\n"))
    }

    fun buildJson() {
       // File("proxies.json").writeText() Sleepy atm
    }

}