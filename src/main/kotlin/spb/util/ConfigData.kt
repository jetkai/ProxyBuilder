package spb.util

import kotlinx.serialization.Serializable

/**
 * @author Kai
 */
@Serializable
class ConfigData(
    val proxyOutputPath : String, val victimTestServerIp : Array<String>, val victimTestServerPort : IntArray,
    val proxyEndpointUrl : String, val proxyEndpointGithubUrl : String, val proxyGithubList : Array<String>,
    val localDatabasePath : String
)