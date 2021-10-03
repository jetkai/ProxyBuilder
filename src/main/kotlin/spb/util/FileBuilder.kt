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

    @ExperimentalSerializationApi
    fun buildCsvFile(
        socks4Array: List<String>, socks5Array: List<String>,
        httpArray: List<String>, httpsArray: List<String>,
        isWritingArchive: Boolean) {

        if (Constants.STAGE.contains("GIT")) return //Prevents writing to the file when uploading to GIT
        try {
            val outPath: Path = if (!isWritingArchive)
                Path.of("${Config.values?.proxyOutputPath}/online-proxies/csv/proxies.csv")
            else
                Path.of("${Config.values?.proxyOutputPath}/archive/csv/working-proxies-history.csv")

            val writer = Files.newBufferedWriter(outPath)

            val format = CSVFormat.Builder.create()
            format.setHeader("SOCKS4", "SOCKS5", "HTTP", "HTTPS")

            val csvPrinter = CSVPrinter(writer, format.build())

            var maxSize = 0
            val socks4Size = socks4Array.size
            val socks5Size = socks5Array.size
            val httpSize = httpArray.size
            val httpsSize = httpsArray.size

            if (socks4Size > maxSize) maxSize = socks4Size
            if (socks5Size > maxSize) maxSize = socks5Size
            if (httpSize > maxSize) maxSize = httpSize
            if (httpsSize > maxSize) maxSize = httpsSize

            for (i in 0 until maxSize) {
                var socks4Value = ""
                var socks5Value = ""
                var httpValue = ""
                var httpsValue = ""
                if (socks4Array.size > i) socks4Value = socks4Array[i]
                if (socks5Array.size > i) socks5Value = socks5Array[i]
                if (httpArray.size > i) httpValue = httpArray[i]
                if (httpsArray.size > i) httpsValue = httpsArray[i]
                csvPrinter.printRecord(socks4Value, socks5Value, httpValue, httpsValue)
            }
            csvPrinter.flush()
            csvPrinter.close()
        } catch (e : Exception) {
            println(e.stackTraceToString())
        }
    }

    fun buildTxtFiles(
        socks4Array: List<String>, socks5Array: List<String>,
        httpArray: List<String>, httpsArray: List<String>,
        isWritingArchive: Boolean) {

        if(Constants.STAGE.contains("GIT")) return

        try {
            if (!isWritingArchive) {
                File("${Config.values?.proxyOutputPath}/online-proxies/txt/proxies-socks4.txt").writeText(socks4Array.joinToString("\n"))
                File("${Config.values?.proxyOutputPath}/online-proxies/txt/proxies-socks5.txt").writeText(socks5Array.joinToString("\n"))
                File("${Config.values?.proxyOutputPath}/online-proxies/txt/proxies-socks4+5.txt").writeText(sortByIp((socks4Array + socks5Array).distinct())
                    .joinToString("\n"))

                File("${Config.values?.proxyOutputPath}/online-proxies/txt/proxies-http.txt").writeText(httpArray.joinToString("\n"))
                File("${Config.values?.proxyOutputPath}/online-proxies/txt/proxies-https.txt").writeText(httpsArray.joinToString("\n"))
                File("${Config.values?.proxyOutputPath}/online-proxies/txt/proxies-http+https.txt").writeText(sortByIp((httpArray + httpsArray).distinct())
                    .joinToString("\n"))

                //All Proxies - De-duped in final list by ip:port
                val allProxies = sortByIp((socks4Array + socks5Array + httpArray + httpsArray).distinct())
                File("${Config.values?.proxyOutputPath}/online-proxies/txt/proxies.txt").writeText(allProxies.joinToString("\n"))
            } else {
                //All Proxies - De-duped in final list by ip:port
                val allProxies = sortByIp((socks4Array + socks5Array + httpArray + httpsArray).distinct())
                File("${Config.values?.proxyOutputPath}/archive/txt/working-proxies-history.txt").writeText(allProxies.joinToString(
                    "\n"))
            }
        } catch (e : Exception) {
            println(e.stackTraceToString())
        }
    }

    @ExperimentalSerializationApi
    fun buildJsonFiles(
        socks4Array: List<String>, socks5Array: List<String>,
        httpArray: List<String>, httpsArray: List<String>,
        isWritingArchive: Boolean) {

        if(Constants.STAGE.contains("GIT")) return

        val rawJson = Json
        val prettyJson = Json { prettyPrint = true; encodeDefaults = true }

        val socksProxies = buildJsonObject {
            putJsonArray("socks4") { socks4Array.forEach { proxy -> add(proxy) } }
            putJsonArray("socks5") { socks5Array.forEach { proxy -> add(proxy) } }
        }

        val httpProxies = buildJsonObject {
            putJsonArray("http") { httpArray.forEach { proxy -> add(proxy) } }
            putJsonArray("https") { httpsArray.forEach { proxy -> add(proxy) } }
        }

        val allProxies = buildJsonObject {
            putJsonArray("socks4") { socks4Array.forEach { proxy -> add(proxy) } }
            putJsonArray("socks5") { socks5Array.forEach { proxy -> add(proxy) } }
            putJsonArray("http") { httpArray.forEach { proxy -> add(proxy) } }
            putJsonArray("https") { httpsArray.forEach { proxy -> add(proxy) } }
        }

        if(!isWritingArchive) {
            //SOCKS 4 & SOCKS 5
            File("${Config.values?.proxyOutputPath}/online-proxies/json/proxies-socks4+5.json").writeText(rawJson.encodeToString(socksProxies))
            File("${Config.values?.proxyOutputPath}/online-proxies/json/proxies-socks4+5-beautify.json").writeText(prettyJson.encodeToString(socksProxies))
            //HTTP & HTTPS
            File("${Config.values?.proxyOutputPath}/online-proxies/json/proxies-http+https.json").writeText(rawJson.encodeToString(httpProxies))
            File("${Config.values?.proxyOutputPath}/online-proxies/json/proxies-http+https-beautify.json").writeText(prettyJson.encodeToString(httpProxies))
            //All Proxies
            File("${Config.values?.proxyOutputPath}/online-proxies/json/proxies.json").writeText(rawJson.encodeToString(allProxies))
            File("${Config.values?.proxyOutputPath}/online-proxies/json/proxies-beautify.json").writeText(prettyJson.encodeToString(allProxies))
        } else {
            //ALL PROXIES HISTORY - ARCHIVE
            File("${Config.values?.proxyOutputPath}/archive/json/working-proxies-history.json").writeText(rawJson.encodeToString(allProxies))
            File("${Config.values?.proxyOutputPath}/archive/json/working-proxies-history-beautify.json").writeText(prettyJson.encodeToString(allProxies))
        }
    }

    fun buildReadmeFile() {
        val readmeFile = File("${Config.values?.proxyOutputPath}/README.md")
        val socks4File = File("${Config.values?.proxyOutputPath}/online-proxies/txt/proxies-socks4.txt")
        val socks5File = File("${Config.values?.proxyOutputPath}/online-proxies/txt/proxies-socks5.txt")
        val httpFile = File("${Config.values?.proxyOutputPath}/online-proxies/txt/proxies-http.txt")
        val httpsFile = File("${Config.values?.proxyOutputPath}/online-proxies/txt/proxies-https.txt")
        val proxiesFile = File("${Config.values?.proxyOutputPath}/online-proxies/txt/proxies.txt")

        val archiveFile = File("${Config.values?.proxyOutputPath}/archive/txt/working-proxies-history.txt")

        if(!readmeFile.exists() || !socks4File.exists() || !socks5File.exists()
            || !httpFile.exists() || !httpsFile.exists() || !proxiesFile.exists()
            || !archiveFile.exists())
            return

        val socks4Size = socks4File.readLines().size
        val socks5Size = socks5File.readLines().size
        val httpSize = httpFile.readLines().size
        val httpsSize = httpsFile.readLines().size

        val readmeText = readmeFile.readText()

        val originalText = readmeText.substring(0, readmeText.indexOf("# [SAMPLE PROXIES]"))
        var extraText = "# [SAMPLE PROXIES] - ${SimpleDateFormat("[MMMM dd yyyy | hh:mm:ss]").format(Date())}\n\n"

        val totalProxiesSize = (socks4Size + socks5Size + httpSize + httpsSize)
        val uniqueTotalProxiesSize = proxiesFile.readLines().size
        val uniqueTotalProxiesArchiveSize = archiveFile.readLines().size
        val gitHubLinkArray = Config.values?.proxyGithubList

        val proxiesInfoText =
            "### Proxy Statistics:\n" +
                    "- _Online Proxies (By Protocol):_\n" +
                    "   - **SOCKS4** -> $socks4Size\n" +
                    "   - **SOCKS5** -> $socks5Size\n" +
                    "   - **HTTP** -> $httpSize\n" +
                    "   - **HTTPS** -> $httpsSize\n\n" +
                    "- _Proxies (Total):_\n" +
                    "   - **Online Proxies (SOCKS4/5 + HTTP/S)** -> $totalProxiesSize\n" +
                    "   - **Unique Online Proxies** -> $uniqueTotalProxiesSize\n" +
                    "   - **Unique Online/Offline Proxies (Archive)** -> $uniqueTotalProxiesArchiveSize\n\n"

        //Append proxiesInfoText to extraText
        extraText += proxiesInfoText

        val codeTextArray = arrayListOf(
            arrayListOf("[SOCKS4 ($socks4Size/$uniqueTotalProxiesSize)](${gitHubLinkArray?.get(0)})",
                socks4File.useLines { l: Sequence<String> -> l.take(30).toMutableList().joinToString(separator = "\n")}),
            arrayListOf("[SOCKS5 ($socks5Size/$uniqueTotalProxiesSize)](${gitHubLinkArray?.get(1)})",
                socks5File.useLines { l: Sequence<String> -> l.take(30).toMutableList().joinToString(separator = "\n")}),
            arrayListOf("[HTTP ($httpSize/$uniqueTotalProxiesSize)](${gitHubLinkArray?.get(2)})",
                httpFile.useLines { l: Sequence<String> -> l.take(30).toMutableList().joinToString(separator = "\n")}),
            arrayListOf("[HTTPS ($httpsSize/$uniqueTotalProxiesSize)](${gitHubLinkArray?.get(3)})",
                httpsFile.useLines { l: Sequence<String> -> l.take(30).toMutableList().joinToString(separator = "\n")}),
            arrayListOf("[ARCHIVE ($uniqueTotalProxiesSize/$uniqueTotalProxiesArchiveSize)](${gitHubLinkArray?.get(4)})",
                archiveFile.useLines { l: Sequence<String> -> l.take(30).toMutableList().joinToString(separator = "\n")})
        )

        for(codeText in codeTextArray) {
            extraText += ("## ${codeText[0]}"
                .plus("\n")
                .plus("```yaml")
                .plus("\n")
                .plus(codeText[1])
                .plus("\n```\n\n"
                ))
        }

        readmeFile.writeText(
            originalText
                .plus(extraText)
                .plus("\n\nThx Co Pure Gs - Sort Meister! \uD83D\uDC9F")
        )
    }

    @ExperimentalSerializationApi
    fun buildProxyArchive(
        socks4Array: List<String>, socks5Array: List<String>,
        httpArray: List<String>, httpsArray: List<String>) {

        if(Constants.STAGE.contains("GIT")) return //Prevents writing to the file when uploading to GIT

        val proxyArchiveUrl = "https://github.com/jetkai/proxy-list/raw/main/archive/json/working-proxies-history.json"
        val proxyArchiveJson = try { URL(proxyArchiveUrl).readText() } catch (e : Exception) {
            println("Issue with connecting to githubusercontent.com:\n${e.message}") }

        val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

        var proxyArchive = ProxyData(arrayOf(), arrayOf(), arrayOf(), arrayOf())
        try {
            proxyArchive = json.decodeFromString<Array<ProxyData>>("[$proxyArchiveJson]").associateBy{ it }.keys.toMutableList()[0]
        } catch (i : Exception) { }

        println("(Compare Archive) START: SOCKS4[${proxyArchive.socks4.size}], SOCKS5[${proxyArchive.socks5.size}], " +
                "HTTP[${proxyArchive.http.size}], HTTPS[${proxyArchive.https.size}]")

        val combinedSocks4 = proxyArchive.socks4.plus(socks4Array).distinct()
        val combinedSocks5 = proxyArchive.socks5.plus(socks5Array).distinct()
        val combinedHttp = proxyArchive.http.plus(httpArray).distinct()
        val combinedHttps = proxyArchive.https.plus(httpsArray).distinct()

        val socks4 = sortByIp(combinedSocks4)
        val socks5 = sortByIp(combinedSocks5)
        val http = sortByIp(combinedHttp)
        val https = sortByIp(combinedHttps)

        println("(Compare Archive) END: SOCKS4[${socks4.size}], SOCKS5[${socks5.size}], " +
                "HTTP[${http.size}], HTTPS[${https.size}]")

        /**
         * WRITE CSV
         */

        buildCsvFile(socks4, socks5, http, https, true)

        /**
         * WRITE JSON
         */

        buildJsonFiles(socks4, socks5, http, https, true)

        /**
         * WRITE TXT
         */

        buildTxtFiles(socks4, socks5, http, https, true)
    }

    fun sortByIp(proxyArray : List<String>) : List<String> {
        val ipComparator: Comparator<String> = Comparator { ip1, ip2 -> toNumeric(ip1).compareTo(toNumeric(ip2)) }
        return proxyArray.sortedWith(ipComparator)
    }

    fun toNumeric(ip: String) : Long {
        var finalIp = ip
        if (finalIp.contains(":"))
            finalIp = finalIp.split(":")[0]
        val finalIpArray = finalIp.split(".")
        return ((finalIpArray[0].toLong() shl 24) + (finalIpArray[1].toLong() shl 16) + (finalIpArray[2].toLong() shl 8)
                + finalIpArray[3].toLong())
    }

}