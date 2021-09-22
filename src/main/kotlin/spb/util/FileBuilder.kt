package spb.util

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonArray
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import spb.Constants
import spb.net.proxy.ProxyData
import spb.net.proxy.VerifiedProxies
import java.io.BufferedWriter
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.*


/**
 * @author Kai
 */
@ExperimentalSerializationApi
object FileBuilder { //TODO - Complete rewrite this entire object file, re-write into class, clean-up & optimize

    private val socks4Array = arrayListOf<String>()
    private val socks5Array = arrayListOf<String>()
    private val httpArray = arrayListOf<String>()
    private val httpsArray = arrayListOf<String>()

    private val fileArray = arrayListOf("proxies-socks4.txt", "proxies-socks5.txt", "proxies-socks4+5.txt",
        "proxies-http.txt", "proxies-https.txt", "proxies-http+https.txt", "proxies.txt",
        "proxies-socks4+5.json", "proxies-socks4+5-beautify.json", "proxies-http+https.json",
        "proxies-http+https-beautify.json", "proxies.json", "proxies-beautify.json")

    fun deleteOldProxyFiles() {
        fileArray.forEach { file -> File("${Config.values?.proxyOutputPath}/$file").deleteRecursively() }
    }

    fun appendTxtFiles(proxy : String, type : String) { //TESTING {FOR TESTING}
        if(Constants.STAGE.contains("GIT")) return //Prevents writing to the file when uploading to GIT

        if(type == "socks4") File("${Config.values?.proxyOutputPath}/proxies-socks4.txt").appendText("$proxy\n")
        if(type == "socks5") File("${Config.values?.proxyOutputPath}/proxies-socks5.txt").appendText("$proxy\n")
        if(type.contains("socks")) File("${Config.values?.proxyOutputPath}/proxies-socks4+5.txt").appendText("$proxy\n")

        if(type == "http") File("${Config.values?.proxyOutputPath}/proxies-http.txt").appendText("$proxy\n")
        if(type == "https") File("${Config.values?.proxyOutputPath}/proxies-https.txt").appendText("$proxy\n")
        if(type.contains("http")) File("${Config.values?.proxyOutputPath}/proxies-http+https.txt").appendText("$proxy\n")

        //All Proxies
        File("${Config.values?.proxyOutputPath}/proxies.txt").appendText("$proxy\n")
    }

    //TODO - Some errors here, need to sleep
    @ExperimentalSerializationApi
    fun appendJsonFiles(fProxy : String, type : String) { //TESTING
        if(Constants.STAGE.contains("GIT")) return //Prevents writing to the file when uploading to GIT

        if(type == "socks4") socks4Array += fProxy
        if(type == "socks5") socks5Array += fProxy
        if(type == "http") httpArray += fProxy
        if(type == "https") httpsArray += fProxy

        val rawJson = Json
        val prettyJson = Json { prettyPrint = true; encodeDefaults = true }

        val socksProxies = buildJsonObject {
            putJsonArray("socks4") { for (proxy in socks4Array) add(proxy) }
            putJsonArray("socks5") { for (proxy in socks5Array) add(proxy) }
        }

        val httpProxies = buildJsonObject {
            putJsonArray("http") { for (proxy in httpArray) add(proxy) }
            putJsonArray("https") { for (proxy in httpsArray) add(proxy) }
        }

        val allProxies = buildJsonObject {
            putJsonArray("socks4") { for (proxy in socks4Array) add(proxy) }
            putJsonArray("socks5") { for (proxy in socks5Array) add(proxy) }
            putJsonArray("http") { for (proxy in httpArray) add(proxy) }
            putJsonArray("https") { for (proxy in httpsArray) add(proxy) }
        }

        File("${Config.values?.proxyOutputPath}/proxies-socks4+5.json").writeText(rawJson.encodeToString(socksProxies))
        File("${Config.values?.proxyOutputPath}/proxies-socks4+5-beautify.json").writeText(prettyJson.encodeToString(socksProxies))

        File("${Config.values?.proxyOutputPath}/proxies-http+https.json").writeText(rawJson.encodeToString(httpProxies))
        File("${Config.values?.proxyOutputPath}/proxies-http+https-beautify.json").writeText(prettyJson.encodeToString(httpProxies))

        File("${Config.values?.proxyOutputPath}/proxies.json").writeText(rawJson.encodeToString(allProxies))
        File("${Config.values?.proxyOutputPath}/proxies-beautify.json").writeText(prettyJson.encodeToString(allProxies))
    }

    @ExperimentalSerializationApi
    fun buildCsvFile(socks4Array: List<String>, socks5Array: List<String>,
                     httpArray: List<String>, httpsArray: List<String>,
                     isWritingArchive: Boolean) { //TESTING
        if(Constants.STAGE.contains("GIT")) return //Prevents writing to the file when uploading to GIT

        val writer : BufferedWriter = if(!isWritingArchive)
            Files.newBufferedWriter(Path.of("${Config.values?.proxyOutputPath}/proxies.csv"))
        else
            Files.newBufferedWriter(Path.of("${Config.values?.proxyOutputPath}/archive/working-proxies-history.csv"))
        
        val format = CSVFormat.Builder.create()
        format.setHeader("SOCKS4", "SOCKS5", "HTTP", "HTTPS")

        val csvPrinter = CSVPrinter(writer, format.build())

        var maxSize = 0
        val socks4Size = socks4Array.size; val socks5Size = socks5Array.size
        val httpSize = httpArray.size; val httpsSize = httpsArray.size

        if(socks4Size > maxSize) maxSize = socks4Size
        if(socks5Size > maxSize) maxSize = socks5Size
        if(httpSize > maxSize) maxSize = httpSize
        if(httpsSize > maxSize) maxSize = httpsSize

        for(i in 0 until maxSize) {
            var socks4Value = "";  var socks5Value = ""; var httpValue = ""; var httpsValue = ""
            if(socks4Array.size > i) socks4Value = socks4Array[i]
            if(socks5Array.size > i) socks5Value = socks5Array[i]
            if(httpArray.size > i) httpValue = httpArray[i]
            if(httpsArray.size > i) httpsValue = httpsArray[i]
            csvPrinter.printRecord(socks4Value, socks5Value, httpValue, httpsValue)
        }
        csvPrinter.flush()
        csvPrinter.close()
    }

    fun updateReadme() {
        val readmeFile = File("${Config.values?.proxyOutputPath}/README.md")
        val socks4File = File("${Config.values?.proxyOutputPath}/proxies-socks4.txt")
        val socks5File = File("${Config.values?.proxyOutputPath}/proxies-socks5.txt")
        val httpFile = File("${Config.values?.proxyOutputPath}/proxies-http.txt")
        val httpsFile = File("${Config.values?.proxyOutputPath}/proxies-https.txt")
        val proxiesFile = File("${Config.values?.proxyOutputPath}/proxies.txt")

        val archiveFile = File("${Config.values?.proxyOutputPath}/archive/working-proxies-history.txt")

        if(!readmeFile.exists() || !socks4File.exists() || !socks5File.exists()
            || !httpFile.exists() || !httpsFile.exists() || !proxiesFile.exists()
            || !archiveFile.exists())
            return

        val readmeText = readmeFile.readText()

        val originalText = readmeText.substring(0, readmeText.indexOf("# [SAMPLE PROXIES]"))
        var newText = "# [SAMPLE PROXIES] - ${SimpleDateFormat("[MMMM dd yyyy | hh:mm:ss]").format(Date())}\n\n"

        val totalProxiesCount = proxiesFile.readLines().size
        val gitHubLinkArray = Config.values?.proxyGithubList

        val codeTextArray = arrayListOf(
            arrayListOf("[SOCKS4 (${socks4File.readLines().size}/$totalProxiesCount)](${gitHubLinkArray?.get(0)})",
                socks4File.useLines { l: Sequence<String> -> l.take(30).toMutableList().joinToString(separator = "\n")}),
            arrayListOf("[SOCKS5 (${socks5File.readLines().size}/$totalProxiesCount)](${gitHubLinkArray?.get(1)})",
                socks5File.useLines { l: Sequence<String> -> l.take(30).toMutableList().joinToString(separator = "\n")}),
            arrayListOf("[HTTP (${httpFile.readLines().size}/$totalProxiesCount)](${gitHubLinkArray?.get(2)})",
                httpFile.useLines { l: Sequence<String> -> l.take(30).toMutableList().joinToString(separator = "\n")}),
            arrayListOf("[HTTPS (${httpsFile.readLines().size}/$totalProxiesCount)](${gitHubLinkArray?.get(3)})",
                httpsFile.useLines { l: Sequence<String> -> l.take(30).toMutableList().joinToString(separator = "\n")}),
            arrayListOf("[ARCHIVE ($totalProxiesCount/${archiveFile.readLines().size})](${gitHubLinkArray?.get(4)})",
                archiveFile.useLines { l: Sequence<String> -> l.take(30).toMutableList().joinToString(separator = "\n")})
        )

        for(codeText in codeTextArray) {
           newText += ("## ${codeText[0]}"
                .plus("\n")
                .plus("```yaml")
                .plus("\n")
                .plus(codeText[1])
                .plus("\n```\n\n"
                )
            )
        }

        readmeFile.writeText(
            originalText
            .plus(newText)
            .plus("\n\nThx Co Pure Gs - Sort miester! \uD83D\uDC9F")
        )
    }


    fun buildTxtFiles(
        socks4Array: List<String>, socks5Array: List<String>,
        httpArray: List<String>, httpsArray: List<String>,
        isWritingArchive: Boolean
    ) { //TESTING


        if(!isWritingArchive) {
            File("${Config.values?.proxyOutputPath}/proxies-socks4.txt").writeText(socks4Array.joinToString("\n"))
            File("${Config.values?.proxyOutputPath}/proxies-socks5.txt").writeText(socks5Array.joinToString("\n"))
            File("${Config.values?.proxyOutputPath}/proxies-socks4+5.txt").writeText((socks4Array + socks5Array).shuffled()
                .joinToString("\n"))

            File("${Config.values?.proxyOutputPath}/proxies-http.txt").writeText(httpArray.joinToString("\n"))
            File("${Config.values?.proxyOutputPath}/proxies-https.txt").writeText(httpsArray.joinToString("\n"))
            File("${Config.values?.proxyOutputPath}/proxies-http+https.txt").writeText((httpArray + httpsArray).shuffled()
                .joinToString("\n"))

            //All Proxies
            val allProxies = (socks4Array + socks5Array + httpArray + httpsArray).shuffled()
            File("${Config.values?.proxyOutputPath}/proxies.txt").writeText(allProxies.joinToString("\n"))
        } else {
            val allProxies = (socks4Array + socks5Array + httpArray + httpsArray)
            File("${Config.values?.proxyOutputPath}/archive/working-proxies-history.txt").writeText(allProxies.joinToString("\n"))
        }

    }

    //TODO - Some errors here, need to sleep
    @ExperimentalSerializationApi
    fun buildJsonFiles(
        socks4Array: List<String>, socks5Array: List<String>, httpArray: List<String>, httpsArray: List<String>,
        isWritingArchive: Boolean,
    ) { //TESTING

        val rawJson = Json
        val prettyJson = Json { prettyPrint = true; encodeDefaults = true }

        val socksProxies = buildJsonObject {
            putJsonArray("socks4") { for (proxy in socks4Array) add(proxy) }
            putJsonArray("socks5") { for (proxy in socks5Array) add(proxy) }
        }

        val httpProxies = buildJsonObject {
            putJsonArray("http") { for (proxy in httpArray) add(proxy) }
            putJsonArray("https") { for (proxy in httpsArray) add(proxy) }
        }

        val allProxies = buildJsonObject {
            putJsonArray("socks4") { for (proxy in socks4Array) add(proxy) }
            putJsonArray("socks5") { for (proxy in socks5Array) add(proxy) }
            putJsonArray("http") { for (proxy in httpArray) add(proxy) }
            putJsonArray("https") { for (proxy in httpsArray) add(proxy) }
        }

        if(!isWritingArchive) {
            //SOCKS 4 & SOCKS 5
            File("${Config.values?.proxyOutputPath}/proxies-socks4+5.json").writeText(rawJson.encodeToString(socksProxies))
            File("${Config.values?.proxyOutputPath}/proxies-socks4+5-beautify.json").writeText(prettyJson.encodeToString(socksProxies))
            //HTTP & HTTPS
            File("${Config.values?.proxyOutputPath}/proxies-http+https.json").writeText(rawJson.encodeToString(httpProxies))
            File("${Config.values?.proxyOutputPath}/proxies-http+https-beautify.json").writeText(prettyJson.encodeToString(httpProxies))
            //All Proxies
            File("${Config.values?.proxyOutputPath}/proxies.json").writeText(rawJson.encodeToString(allProxies))
            File("${Config.values?.proxyOutputPath}/proxies-beautify.json").writeText(prettyJson.encodeToString(allProxies))
        } else {
            //ALL PROXIES HISTORY - ARCHIVE
            File("${Config.values?.proxyOutputPath}/archive/working-proxies-history.json").writeText(rawJson.encodeToString(allProxies))
            File("${Config.values?.proxyOutputPath}/archive/working-proxies-history-beautify.json").writeText(prettyJson.encodeToString(allProxies))
        }
    }

    @ExperimentalSerializationApi
    fun buildProxyArchive() { //TESTING
        if(Constants.STAGE.contains("GIT")) return //Prevents writing to the file when uploading to GIT

        val proxyArchiveUrl = "https://github.com/jetkai/proxy-list/raw/main/archive/working-proxies-history.json"
        val proxyArchiveJson = try { URL(proxyArchiveUrl).readText() } catch (e : Exception) {
            println("Issue with connecting to githubusercontent.com:\n${e.message}") }

        val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

        var proxyArchive = ProxyData(arrayOf(), arrayOf(), arrayOf(), arrayOf())
        try {
            proxyArchive = json.decodeFromString<Array<ProxyData>>("[$proxyArchiveJson]").associateBy{ it }.keys.toMutableList()[0]
        } catch (i : Exception) { }

        val socks4Array = sortByIp(proxyArchive.socks4.plus(VerifiedProxies.socks4).distinct())
        val socks5Array = sortByIp(proxyArchive.socks5.plus(VerifiedProxies.socks5).distinct())
        val httpArray = sortByIp(proxyArchive.http.plus(VerifiedProxies.http).distinct())
        val httpsArray = sortByIp(proxyArchive.https.plus(VerifiedProxies.https).distinct()) //Test Sorting

        /**
         * WRITE CSV
         */

        buildCsvFile(socks4Array, socks5Array, httpArray, httpsArray, true)

        /**
         * WRITE JSON
         */

        buildJsonFiles(socks4Array, socks5Array, httpArray, httpsArray, true)

        /**
         * WRITE TXT
         */

        buildTxtFiles(socks4Array, socks5Array, httpArray, httpsArray, true)
    }

    fun sortByIp(proxyArray : List<String>): List<String> {
        return proxyArray.sortedWith { o1, o2 ->
            val ip = o1.split(":")[0].split(".").toTypedArray()
            var format = ""
            try {
                format = String.format("%3s.%3s.%3s.%3s", ip[0], ip[1], ip[2], ip[3])
            } catch (e : ArrayIndexOutOfBoundsException) {
                println("Issue with: ${ip.joinToString()}")
            }
            val ip2 = o2.split(":")[0].split(".").toTypedArray()
            var format2 = ""
            try {
                format2 = String.format("%3s.%3s.%3s.%3s", ip2[0], ip2[1], ip2[2], ip2[3])
            } catch (e : ArrayIndexOutOfBoundsException) {
                println("Issue with: ${ip2.joinToString()}")
            }
            format.compareTo(format2)
        }
    }

}