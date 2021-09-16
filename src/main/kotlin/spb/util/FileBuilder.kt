package spb.util

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonArray
import spb.Constants
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author Kai
 */
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

    fun updateReadme() {
        val readmeFile = File("${Config.values?.proxyOutputPath}/README.md")
        val socks4File = File("${Config.values?.proxyOutputPath}/proxies-socks4.txt")
        val socks5File = File("${Config.values?.proxyOutputPath}/proxies-socks5.txt")
        val httpFile = File("${Config.values?.proxyOutputPath}/proxies-http.txt")
        val httpsFile = File("${Config.values?.proxyOutputPath}/proxies-https.txt")
        val proxiesFile = File("${Config.values?.proxyOutputPath}/proxies.txt")

        if(!readmeFile.exists() || !socks4File.exists() || !socks5File.exists()
            || !httpFile.exists() || !httpsFile.exists() || !proxiesFile.exists())
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
                httpsFile.useLines { l: Sequence<String> -> l.take(30).toMutableList().joinToString(separator = "\n")})
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


    fun buildTxtFiles() { //TESTING

        File("${Config.values?.proxyOutputPath}/proxies-socks4.txt").writeText(socks4Array.joinToString("\n"))
        File("${Config.values?.proxyOutputPath}/proxies-socks5.txt").writeText(socks5Array.joinToString("\n"))
        File("${Config.values?.proxyOutputPath}/proxies-socks4+5.txt").writeText((socks4Array+socks5Array).shuffled().joinToString("\n"))

        File("${Config.values?.proxyOutputPath}/proxies-http.txt").writeText(httpArray.joinToString("\n"))
        File("${Config.values?.proxyOutputPath}/proxies-https.txt").writeText(httpsArray.joinToString("\n"))
        File("${Config.values?.proxyOutputPath}/proxies-http+https.txt").writeText((httpArray+httpsArray).shuffled().joinToString("\n"))

        //All Proxies
        val allProxies = (socks4Array + socks5Array + httpArray + httpsArray).shuffled()
        File("${Config.values?.proxyOutputPath}/proxies.txt").writeText(allProxies.joinToString("\n"))

    }

    //TODO - Some errors here, need to sleep
    @ExperimentalSerializationApi
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

        File("${Config.values?.proxyOutputPath}/proxies-socks4+5.json").writeText(rawJson.encodeToString(socksProxies))
        File("${Config.values?.proxyOutputPath}/proxies-socks4+5-beautify.json").writeText(prettyJson.encodeToString(socksProxies))

        File("${Config.values?.proxyOutputPath}/proxies-http+https.json").writeText(rawJson.encodeToString(httpProxies))
        File("${Config.values?.proxyOutputPath}/proxies-http+https-beautify.json").writeText(prettyJson.encodeToString(httpProxies))

        File("${Config.values?.proxyOutputPath}/proxies.json").writeText(rawJson.encodeToString(allProxies))
        File("${Config.values?.proxyOutputPath}/proxies-beautify.json").writeText(prettyJson.encodeToString(allProxies))
    }

}