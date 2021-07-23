package spb.util

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import spb.net.proxy.ProxyOutData
import java.io.File

class FileBuilder { //TESTING

    private val test = arrayListOf("123.123.123.123:8080", "444.111.444.111:0992", "127.0.0.1:80")

    companion object {
    @JvmStatic
        fun main(args:Array<String>) {
           // FileBuilder().buildTxt()
            FileBuilder().buildJson()
        }
    }

    fun buildTxt() { //TESTING
        File("proxies.txt").writeText(test.joinToString("\n"))
    }

    //TODO - Some errors here, need to sleep
    fun buildJson() { //TESTING
        val json = Json

        for(x in test) {
            val proxyAddr = x.split(":")
            json.encodeToJsonElement(ProxyOutData.serializer(), value = ProxyOutData(proxyAddr[0], Integer.parseInt(proxyAddr[1]), "SOCKS5"))
        }

        val encodedData = json.encodeToString(ProxyOutData.serializer())
        File("proxies.json").writeText(encodedData)
    }

}