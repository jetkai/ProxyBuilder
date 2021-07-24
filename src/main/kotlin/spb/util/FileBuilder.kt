package spb.util

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonArray
import spb.Constants
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object FileBuilder { //TESTING

    private val socks4Array = arrayListOf<String>(/*"123.123.123.123:8080", "444.111.444.111:0992", "127.0.0.1:80"*/)
    private val socks5Array = arrayListOf<String>(/*"123.123.123.123:8080", "444.111.444.111:0992", "127.0.0.1:80"*/)
    private val httpArray = arrayListOf<String>(/*"123.123.123.123:8080", "444.111.444.111:0992", "127.0.0.1:80"*/)
    private val httpsArray = arrayListOf<String>(/*"123.123.123.123:8080", "444.111.444.111:0992", "127.0.0.1:80"*/)

    private val fileArray = arrayListOf("proxies-socks4.txt", "proxies-socks5.txt", "proxies-socks4+5.txt",
        "proxies-http.txt", "proxies-https.txt", "proxies-http+https.txt", "proxies.txt",
        "proxies-socks4+5.json", "proxies-socks4+5-beautify.json", "proxies-http+https.json",
        "proxies-http+https-beautify.json", "proxies.json", "proxies-beautify.json")

    fun deleteOldProxyFiles() {
        fileArray.forEach { file -> File("${Constants.MY_SECRET_LOCAL_PATH}\\$file").deleteRecursively() }
    }

    fun appendTxtFiles(proxy : String, type : String) { //TESTING {FOR TESTING}
        if(type == "socks4")
            File("${Constants.MY_SECRET_LOCAL_PATH}\\proxies-socks4.txt").appendText("$proxy\n")
        if(type == "socks5")
            File("${Constants.MY_SECRET_LOCAL_PATH}\\proxies-socks5.txt").appendText("$proxy\n")
        if(type.contains("socks"))
            File("${Constants.MY_SECRET_LOCAL_PATH}\\proxies-socks4+5.txt").appendText("$proxy\n")

        if(type == "http")
            File("${Constants.MY_SECRET_LOCAL_PATH}\\proxies-http.txt").appendText("$proxy\n")
        if(type == "https")
            File("${Constants.MY_SECRET_LOCAL_PATH}\\proxies-https.txt").appendText("$proxy\n")
        if(type.contains("http"))
            File("${Constants.MY_SECRET_LOCAL_PATH}\\proxies-http+https.txt").appendText("$proxy\n")

        //All Proxies
        File("${Constants.MY_SECRET_LOCAL_PATH}\\proxies.txt").appendText("$proxy\n")
    }

    //TODO - Some errors here, need to sleep
    fun appendJsonFiles(fProxy : String, type : String) { //TESTING

        if(type == "socks4")
            socks4Array += fProxy
        if(type == "socks5")
            socks5Array += fProxy
        if(type == "http")
            httpArray += fProxy
        if(type == "https")
            httpsArray += fProxy

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

        File("${Constants.MY_SECRET_LOCAL_PATH}\\proxies-socks4+5.json").writeText(rawJson.encodeToString(socksProxies))
        File("${Constants.MY_SECRET_LOCAL_PATH}\\proxies-socks4+5-beautify.json").writeText(prettyJson.encodeToString(socksProxies))

        File("${Constants.MY_SECRET_LOCAL_PATH}\\proxies-http+https.json").writeText(rawJson.encodeToString(httpProxies))
        File("${Constants.MY_SECRET_LOCAL_PATH}\\proxies-http+https-beautify.json").writeText(prettyJson.encodeToString(httpProxies))

        File("${Constants.MY_SECRET_LOCAL_PATH}\\proxies.json").writeText(rawJson.encodeToString(allProxies))
        File("${Constants.MY_SECRET_LOCAL_PATH}\\proxies-beautify.json").writeText(prettyJson.encodeToString(allProxies))
    }

    fun updateReadme() {
        val readmeFile = File("${Constants.MY_SECRET_LOCAL_PATH}\\README.md")
        val socks4File = File("${Constants.MY_SECRET_LOCAL_PATH}\\proxies-socks4.txt")
        val socks5File = File("${Constants.MY_SECRET_LOCAL_PATH}\\proxies-socks5.txt")
        val httpFile = File("${Constants.MY_SECRET_LOCAL_PATH}\\proxies-http.txt")
        val httpsFile = File("${Constants.MY_SECRET_LOCAL_PATH}\\proxies-https.txt")
        if(!readmeFile.exists() || !socks4File.exists() || !socks5File.exists() || !httpFile.exists() || !httpsFile.exists())
            return
        val readmeText = readmeFile.readText()

        val originalText = readmeText.substring(0, readmeText.indexOf("## [SAMPLE PROXIES]"))
        var newText = "## [SAMPLE PROXIES] - ${SimpleDateFormat("[MMMM dd yyyy | hh:mm:ss]").format(Date())}\n\n"

        val codeTextArray = arrayListOf(
            arrayListOf("SOCKS4", socks4File.useLines { l: Sequence<String> -> l.take(30).toMutableList().joinToString(separator = "\n")}),
            arrayListOf("SOCKS5", socks5File.useLines { l: Sequence<String> -> l.take(30).toMutableList().joinToString(separator = "\n")}),
            arrayListOf("HTTP", httpFile.useLines { l: Sequence<String> -> l.take(30).toMutableList().joinToString(separator = "\n")}),
            arrayListOf("HTTPS", httpsFile.useLines { l: Sequence<String> -> l.take(30).toMutableList().joinToString(separator = "\n")})
        )
        for(codeText in codeTextArray) {
           newText += ("## ${codeText[0]}"
                .plus("\n")
                .plus("```yaml")
                .plus("\n")
                .plus(codeText[1])
                .plus("```\n\n"
                )
            )
        }
        readmeFile.writeText(originalText.plus(newText).plus("\n\nThx Co Pure Gs - Sort miester! \uD83D\uDC9F"))
    }


    fun buildTxtFiles() { //TESTING

        File("${Constants.MY_SECRET_LOCAL_PATH}\\proxies-socks4.txt").writeText(socks4Array.joinToString("\n"))
        File("${Constants.MY_SECRET_LOCAL_PATH}\\proxies-socks5.txt").writeText(socks5Array.joinToString("\n"))
        File("${Constants.MY_SECRET_LOCAL_PATH}\\proxies-socks4+5.txt").writeText((socks4Array+socks5Array).shuffled().joinToString("\n"))

        File("${Constants.MY_SECRET_LOCAL_PATH}\\proxies-http.txt").writeText(httpArray.joinToString("\n"))
        File("${Constants.MY_SECRET_LOCAL_PATH}\\proxies-https.txt").writeText(httpsArray.joinToString("\n"))
        File("${Constants.MY_SECRET_LOCAL_PATH}\\proxies-http+https.txt").writeText((httpArray+httpsArray).shuffled().joinToString("\n"))

        //All Proxies
        val allProxies = (socks4Array + socks5Array + httpArray + httpsArray).shuffled()
        File("${Constants.MY_SECRET_LOCAL_PATH}\\proxies.txt").writeText(allProxies.joinToString("\n"))

    }

    //TODO - Some errors here, need to sleep
    fun buildJsonFiles() { //TESTING
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

        File("${Constants.MY_SECRET_LOCAL_PATH}\\proxies-socks4+5.json").writeText(rawJson.encodeToString(socksProxies))
        File("${Constants.MY_SECRET_LOCAL_PATH}\\proxies-socks4+5-beautify.json").writeText(prettyJson.encodeToString(socksProxies))

        File("${Constants.MY_SECRET_LOCAL_PATH}\\proxies-http+https.json").writeText(rawJson.encodeToString(httpProxies))
        File("${Constants.MY_SECRET_LOCAL_PATH}\\proxies-http+https-beautify.json").writeText(prettyJson.encodeToString(httpProxies))

        File("${Constants.MY_SECRET_LOCAL_PATH}\\proxies.json").writeText(rawJson.encodeToString(allProxies))
        File("${Constants.MY_SECRET_LOCAL_PATH}\\proxies-beautify.json").writeText(prettyJson.encodeToString(allProxies))
    }

}