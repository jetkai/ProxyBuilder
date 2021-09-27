package spb.net.proxy.localdb

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import spb.net.proxy.ProxyData
import spb.util.Config
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

@ExperimentalSerializationApi
class LocalProxyDatabase {

    fun load() {
        val localProxyDatabase = Files.readString(Path.of(Config.values?.localDatabasePath))
        val publicProxyDatabase = Files.readString(Path.of(Config.values?.proxyOutputPath + "/archive/json/working-proxies-history.json"))

        val data = Json { this.encodeDefaults = true; this.ignoreUnknownKeys = true; this.coerceInputValues = true }

        val localProxies = data.decodeFromString<Array<LocalProxyData>>(localProxyDatabase).associateBy{ it }.keys.toMutableList()
        val publicProxies = data.decodeFromString<Array<ProxyData>>("[$publicProxyDatabase]").associateBy{ it }.keys.toMutableList()[0]

        val finalProxyArray = getMasterProxyData(localProxies, publicProxies)
        writeToFile(finalProxyArray)
    }

    private fun getMasterProxyData(localProxies : MutableList<LocalProxyData>, publicProxies : ProxyData) : ArrayList<MasterProxyData> {
        val masterProxyDataArray = arrayListOf<MasterProxyData>()
        appendMasterDataArray(masterProxyDataArray, "SOCKS4", localProxies, publicProxies.socks4)
        appendMasterDataArray(masterProxyDataArray, "SOCKS5", localProxies, publicProxies.socks5)
        appendMasterDataArray(masterProxyDataArray, "HTTP", localProxies, publicProxies.http)
        appendMasterDataArray(masterProxyDataArray, "HTTPS", localProxies, publicProxies.https)
        return masterProxyDataArray
    }

    private fun writeToFile(masterProxyData : ArrayList<MasterProxyData>) {
        val outPath = "C:/Users/Kai/IntelliJProjects/SocksProxyBuilder/data/proxies/working-proxies-history-geo.json"
        val json = Json { this.encodeDefaults = true; this.ignoreUnknownKeys = true }
        val combined = json.encodeToString(masterProxyData)
        File(outPath).writeText(combined)
    }

    //Very inefficient, TODO - Update this
    private fun appendMasterDataArray(masterProxyDataArray: ArrayList<MasterProxyData>, protocol: String,
                                      localProxies: MutableList<LocalProxyData>, publicProxies: Array<String>) {
        for(publicProxy in publicProxies) {
            val ip = publicProxy.split(":")[0]
            val port = publicProxy.split(":")[1].toInt()
            for(localProxy in localProxies) {
                if(localProxy.ip == ip) {
                    val existingDataArray = masterProxyDataArray.filter {  //TODO <- BAD
                        (it.ip == localProxy.ip && it.port != port) || (it.ip == localProxy.ip && it.protocol != protocol)
                    }
                    val detected = localProxy.proxy == 1
                    val masterProxyData = MasterProxyData(ip, port, intArrayOf(port), protocol, arrayOf(protocol), detected, localProxy.type,
                        localProxy.risk, localProxy.asn, localProxy.provider, localProxy.continent, localProxy.country,
                        localProxy.isocode, localProxy.region, localProxy.regioncode, localProxy.city, localProxy.latitude,
                        localProxy.longitude, localProxy.timestamp)
                    if(existingDataArray.isNotEmpty()) {
                        for(modifiedExistingData in existingDataArray) { //TODO <- BAD
                            val index = masterProxyDataArray.indexOf(modifiedExistingData)
                            if (!modifiedExistingData.ports?.contains(port)!!)
                                modifiedExistingData.ports = modifiedExistingData.ports?.plus(port)
                            if (!modifiedExistingData.protocols.contains(protocol))
                                modifiedExistingData.protocols = modifiedExistingData.protocols.plus(protocol)

                            masterProxyDataArray[index] = modifiedExistingData
                        }
                    } else {
                        masterProxyDataArray.add(masterProxyData)
                    }
                }
                continue
            }
        }
    }

}