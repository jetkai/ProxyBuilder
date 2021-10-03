import kotlinx.serialization.ExperimentalSerializationApi
import spb.Constants
import spb.net.proxy.localdb.LocalProxyDatabase
import spb.util.Config

@ExperimentalSerializationApi
class UpdateGeoProxiesList {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            UpdateGeoProxiesList().setConstants()
            Config.init()
            LocalProxyDatabase().load()
        }
    }

    private fun setConstants() {
        Constants.DEBUG_MODE = true
        Constants.DISPLAY_SUCCESS_CONNECTION_MESSAGE = true
        Constants.IS_PROXY_BUILDER_USER = true
    }
}