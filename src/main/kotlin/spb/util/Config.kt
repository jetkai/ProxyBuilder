package spb.util

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import spb.Constants
import java.io.File

object Config {

    var values : ConfigData ?= null

    fun init() {
        val configData = when {
            !Constants.IS_PROXY_BUILDER_USER -> File("${System.getProperty("user.home")}/IntelliJProjects/secrets/config.json").readText()
            else -> this.javaClass.getResource("config.json")?.readText()
        }
        val data = Json { this.encodeDefaults = true; this.ignoreUnknownKeys = true }
        values = data.decodeFromString<Array<ConfigData>>("[$configData]").associateBy{ it }.keys.toMutableList()[0]
    }

}