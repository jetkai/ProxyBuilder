package spb.net.proxy

import kotlinx.serialization.Serializable

/**
 * @author Kai
 */
@Serializable
class ProxyData(val http : Array<String>, val https : Array<String>, val socks4 : Array<String>, val socks5 : Array<String>) {
}