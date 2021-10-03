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
            val checkSortFormatSpeed = CheckSortFormatSpeed()
            checkSortFormatSpeed.setConstants()
            Config.init()
            checkSortFormatSpeed.create()
        }
    }

    private fun setConstants() {
        Constants.DEBUG_MODE = true
        Constants.DISPLAY_SUCCESS_CONNECTION_MESSAGE = true
        Constants.DISPLAY_FAILED_CONNECTION_MESSAGE = false
        Constants.IS_PROXY_BUILDER_USER = true
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