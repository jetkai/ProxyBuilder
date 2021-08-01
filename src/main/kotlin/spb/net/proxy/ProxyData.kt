package spb.net.proxy

import kotlinx.serialization.Serializable

/**
 * @author Kai
 */
@Serializable
class ProxyData(var http : Array<String>, var https : Array<String>, var socks4 : Array<String>, var socks5 : Array<String>)