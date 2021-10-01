import kotlinx.serialization.ExperimentalSerializationApi
import spb.Constants
import spb.util.Config
import spb.util.FileBuilder
import java.util.concurrent.TimeUnit

@ExperimentalSerializationApi
class CheckSortFormatSpeed {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            CheckSortFormatSpeed().setConstants()
            Config.init()
        }
    }

    private fun setConstants() {
        Constants.DEBUG_MODE = true
        Constants.DISPLAY_CONNECTION_MESSAGE = true
        Constants.IS_PROXY_BUILDER_USER = true
    }

    private fun formatIp(ip: String): String {
        var finalIp = ip
        if (finalIp.contains(":"))
            finalIp = finalIp.split(":").toTypedArray()[0]
        val ipArray = finalIp.split(".").toTypedArray()
        for (i in 0..3) {
            if (ipArray[i].startsWith("0") && ipArray[i] != "0")
                ipArray[i] = ipArray[i].substring(1)
        }
        return ipArray.joinToString(separator = ".")
    }

    fun create() {
        val startTime = System.currentTimeMillis()
        FileBuilder.buildProxyArchive(arrayListOf("1.0.0.9", "1.0.3.1", "1.0.1.4", "1.0.0.0"),
            arrayListOf(), arrayListOf(), arrayListOf())
        val endTime = System.currentTimeMillis()
        val finalTime = endTime - startTime
        val formatTime = String.format("%d min, %d sec",
            TimeUnit.MILLISECONDS.toMinutes(finalTime),
            TimeUnit.MILLISECONDS.toSeconds(finalTime) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(finalTime))
        )
        println()
        println(formatTime)
    }

}