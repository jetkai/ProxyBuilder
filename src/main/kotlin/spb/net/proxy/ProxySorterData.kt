package spb.net.proxy

import kotlinx.serialization.Serializable

/**
 * @author Kai
 */
@Serializable
class ProxySorterData(var http : Array<String>, var https : Array<String>, var socks4 : Array<String>, var socks5 : Array<String>, var timestamp : Long? = 0L)