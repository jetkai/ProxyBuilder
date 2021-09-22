package spb.github

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers
import java.nio.file.Files
import java.nio.file.Path

/**
 * @author Kai
 */
class GitHubApi {

    private val releasesApiUrl = "https://api.github.com/repos/jetkai/proxy-list/releases"
    private val releasesApiParams = "?&per_page=100&page="
    private val userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.64 Safari/537.11"

    private val gitHubLinks = arrayListOf<String>()

    companion object {
        @ExperimentalSerializationApi
        @JvmStatic
        fun main(args : Array<String>) {
            GitHubApi().init()
        }
    }
    
    @ExperimentalSerializationApi
    private fun init() {
        request()
        download()
    }

    @ExperimentalSerializationApi
    fun request() {
        val client = HttpClient.newBuilder().build()
        var pageNumber = 0
        while(true) {
            val builder = HttpRequest.newBuilder()
            builder.header("Content-Type", "application/json")
            builder.header("User-Agent", userAgent)
            val request = builder
                .uri(URI.create(releasesApiUrl + releasesApiParams + pageNumber))
                .build()
            
            val response = client.send(request, BodyHandlers.ofString())
            val responseBody = response.body()
            if(responseBody.length < 10)
                break

            val data = Json { this.encodeDefaults = true; this.ignoreUnknownKeys = true }

            try {
                val jsonData = data.decodeFromString<Array<GitHubData>>(responseBody)
                val jsonMap = jsonData.associateBy { it.html_url }

                gitHubLinks += jsonMap.keys

            } catch (e : Exception) {
                val message = e.message
                val isRateLimited = message?.contains("rate-limiting") == true
                when {
                    isRateLimited -> println("Rate Limited by GitHub / CloudFlare.")
                    else -> println(message)
                }
                break
            }
            Thread.sleep(2000)
            println("Completed page $pageNumber")
            pageNumber++
        }
        println("Total links: ${gitHubLinks.size}")
    }

    private fun download() {
        //https://github.com/jetkai/proxy-list/releases/tag/210911-15 [ORIGINAL]
        //https://github.com/jetkai/proxy-list/archive/refs/tags/210911-15.zip [FINAL]
        val client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build()
        gitHubLinks.distinct().forEach { url ->

            val builder = HttpRequest.newBuilder()
            builder.header("User-Agent", userAgent)

            val zipName = url.replace("https://github.com/jetkai/proxy-list/releases/tag/", "") + ".zip"
            val downloadUrl = "https://github.com/jetkai/proxy-list/archive/refs/tags/$zipName"
            val outputPath = "data/proxies/compressed/"

            if(Files.exists(Path.of(outputPath + zipName))) {
                println("$outputPath$zipName already exists.")
            } else {
                val request = builder.uri(URI.create(downloadUrl)).build()
                val inStream = client.sendAsync(request, BodyHandlers.ofInputStream())
                    .thenApply { response: HttpResponse<InputStream> -> response.body() }.join()

                FileOutputStream(outputPath + zipName).use { output -> inStream.transferTo(output) }
            }
        }
    }

}