package spb.util

import kotlinx.serialization.Serializable

/**
 * @author Kai
 */
@Serializable
class ConfigData(
    val proxyOutputPath : String, val victimTestServerIp : String, val victimTestServerPort : Int,
    val victimBackupServerIp : String, val victimBackupServerPort : Int, val proxyEndpointUrl : String,
    val proxyEndpointGithubUrl : String, val proxyGithubList : Array<String>
)