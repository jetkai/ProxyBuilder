package spb.net.proxy

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import spb.Constants
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.sql.Timestamp
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipFile

/**
 * @author Kai
 */
@ExperimentalSerializationApi
class ProxySorter {

    private val proxyDataArray = arrayListOf<ProxySorterData>()

    companion object {
        @JvmStatic
        fun main(args : Array<String>) {
            ProxySorter().search()
        }
    }

    fun search() {
        val rootDir = "data/proxies/compressed/"
        for(file in File(rootDir).list()!!) {
            val zipFile = ZipFile(rootDir + file)
            readZipStream(zipFile)
        }
        writeToFile(combined())
        println("finished")
    }

    @Throws(IOException::class)
    private fun readZipStream(zipFile : ZipFile) {

        val entries = zipFile.entries()

        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()
            val entryName = entry.name.toString()
            if(entryName.contains("proxies.json"))
                addToArray(zipFile.name.substringAfterLast("\\"), zipFile.getInputStream(entry))
        }
    }

    private fun addToArray(fileName : String, contentsIn: InputStream) {
        var proxyData = ProxySorterData(arrayOf(), arrayOf(), arrayOf(), arrayOf(), 0L)
        val proxyDataJson = contentsIn.bufferedReader().use { it.readText() }
        val json = Json

        try {
            proxyData = json.decodeFromString<Array<ProxySorterData>>("[$proxyDataJson]").associateBy { it }.keys.toMutableList()[0]
        } catch (i : Exception) { }

        val rawDate = fileName.replace("proxy-list-", "").replace(".zip", "")
        val df : DateFormat = SimpleDateFormat("yyMMdd-HH")
        try {
            proxyData.timestamp = Timestamp((df.parse(rawDate) as Date).time).time
        } catch (exception : ParseException) {
            proxyData.timestamp = 0
        }

        if(proxyData.socks4.isNotEmpty())
            proxyDataArray.add(proxyData)
    }

    private fun combined() : ProxySorterData {
        val proxyData = ProxySorterData(arrayOf(), arrayOf(), arrayOf(), arrayOf(), 0L)

        for(proxy in proxyDataArray) {
            proxyData.socks4 += proxy.socks4
            proxyData.socks5 += proxy.socks5
            proxyData.http += proxy.http
            proxyData.https += proxy.https
        }

        proxyData.socks4 = proxyData.socks4.distinct().toTypedArray()
        proxyData.socks5 = proxyData.socks5.distinct().toTypedArray()
        proxyData.http = proxyData.http.distinct().toTypedArray()
        proxyData.https = proxyData.https.distinct().toTypedArray()

        return proxyData

    }

    private fun writeToFile(proxies : ProxySorterData) {
        val json = Json {
            this.prettyPrint = true
            this.encodeDefaults = true
            this.ignoreUnknownKeys = true
        }
        val combined = json.encodeToString(proxies)
        File("${Constants.PROXY_BUILDER_DATA_LOCATION}/data/proxies/combined-proxies.json").writeText(combined)
    }

}