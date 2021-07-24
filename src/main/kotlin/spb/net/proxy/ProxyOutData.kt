package spb.net.proxy

import kotlinx.serialization.Serializable

/**
 * @author Kai
 */
@Serializable
class ProxyOutData(val ip : String, val port : Int, val type : String)