package spb.net.proxy

/**
 * @author Kai
 */
object VerifiedProxies {

    var socks4 = arrayListOf<String>() //Working SOCKS4 proxies in memory
    var socks5 = arrayListOf<String>() //Working SOCKS5 proxies in memory
    var http = arrayListOf<String>() //Working HTTP proxies in memory
    var https = arrayListOf<String>() //Working HTTPS proxies in memory

}