import kotlinx.serialization.ExperimentalSerializationApi
import spb.net.proxy.VerifiedProxies
import spb.util.Config
import spb.util.FileBuilder

@ExperimentalSerializationApi
fun main() {
    Config.init()
    create()
}

@ExperimentalSerializationApi
fun create() {
    //verifiedProxiesTestBatch()
    FileBuilder.buildProxyArchive()
    //FileBuilder.buildCsvFile()
    //FileBuilder.buildTxtFiles()
    //FileBuilder.buildJsonFiles()
}

fun verifiedProxiesTestBatch() {
    VerifiedProxies.socks4 += "SOCKS4:1"
    VerifiedProxies.socks4 += "SOCKS4:2"
    VerifiedProxies.socks5 += "SOCKS5:1"
    VerifiedProxies.http += "HTTP:1"
    VerifiedProxies.http += "HTTP:2"
    VerifiedProxies.http += "HTTP:3"
    VerifiedProxies.http += "HTTP:4"
    VerifiedProxies.http += "HTTP:5"
    VerifiedProxies.https += "HTTPS:1"
    VerifiedProxies.https += "HTTPS:2"
}

