package spb.net.proxy.localdb

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import spb.net.proxy.ProxyData
import spb.util.Config
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors
import kotlin.system.exitProcess

@ExperimentalSerializationApi
class LocalProxyDatabase {

    fun load() {
        val startTime = System.currentTimeMillis()
        val localProxyDatabase = Files.readString(Path.of(Config.values?.localDatabasePath))
        val publicProxyDatabase =
            Files.readString(Path.of(Config.values?.proxyOutputPath + "/archive/json/working-proxies-history.json"))

        val data = Json { this.encodeDefaults = true; this.ignoreUnknownKeys = true; this.coerceInputValues = true }

        val localProxies =
            data.decodeFromString<Array<LocalProxyData>>(localProxyDatabase).associateBy { it }.keys.toMutableList()
        val publicProxies = data.decodeFromString<Array<ProxyData>>("[$publicProxyDatabase]")
            .associateBy { it }.keys.toMutableList()[0]

        val finalProxyArray = getMasterProxyData(localProxies, publicProxies).stream()
            .sorted(Comparator.comparingLong(MasterProxyData::ipToLong))
            .collect(Collectors.toList()) as ArrayList<MasterProxyData>

        //Removes duplicates & sorts by severity 1 -> 4
        writeToJsonFile(finalProxyArray)
        writeToCsvFile(finalProxyArray)
        println(runtime(startTime, System.currentTimeMillis()))
        exitProcess(0)
    }

    fun toNumeric(ip: String) : Long {
        var finalIp = ip
        if (finalIp.contains(":"))
            finalIp = finalIp.split(":")[0]
        val finalIpArray = finalIp.split(".")
        return ((finalIpArray[0].toLong() shl 24) + (finalIpArray[1].toLong() shl 16) + (finalIpArray[2].toLong() shl 8)
                + finalIpArray[3].toLong())
    }

    private fun getMasterProxyData(localProxies : MutableList<LocalProxyData>, publicProxies : ProxyData) : ArrayList<MasterProxyData> {
        val masterProxyDataArray = arrayListOf<MasterProxyData>()
        //val test = arrayOf<Pair<String, Array<String>>>("SOCKS4", publicProxies.socks4)
        /*    val protocols = arrayOf(
                arrayOf("SOCKS4", publicProxies.socks4),
                arrayOf("SOCKS5", publicProxies.socks5),
                arrayOf("HTTP", publicProxies.http),
                arrayOf("HTTPS", publicProxies.https))
            for (protocol in protocols) {
                val protocolType = protocol[0] as String
                val proxyArray = protocol[1] as Array<String>
                appendMasterDataArray(masterProxyDataArray, protocolType, localProxies, proxyArray)
            }*/
        appendMasterDataArray(masterProxyDataArray, "SOCKS4", localProxies, publicProxies.socks4)
        appendMasterDataArray(masterProxyDataArray, "SOCKS5", localProxies, publicProxies.socks5)
        appendMasterDataArray(masterProxyDataArray, "HTTP", localProxies, publicProxies.http)
        appendMasterDataArray(masterProxyDataArray, "HTTPS", localProxies, publicProxies.https)
        return masterProxyDataArray
    }

    private fun writeToJsonFile(masterProxyData : ArrayList<MasterProxyData>) {
        val outPath = "C:/Users/Kai/IntelliJProjects/SocksProxyBuilder/data/proxies/working-proxies-history-geo.json"
        val json = Json { this.encodeDefaults = true; this.ignoreUnknownKeys = true }
        val rawJson = json.encodeToString(masterProxyData)

        val outPath2 = "C:/Users/Kai/IntelliJProjects/SocksProxyBuilder/data/proxies/working-proxies-history-geo-pretty.json"
        val json2 = Json { this.encodeDefaults = true; this.ignoreUnknownKeys = true; this.prettyPrint = true }
        val prettyJson = json2.encodeToString(masterProxyData)

        File(outPath).writeText(rawJson)
        File(outPath2).writeText(prettyJson)
    }

    private fun writeToCsvFile(masterProxyData : ArrayList<MasterProxyData>) {
        val outPath = Path.of(
            "C:/Users/Kai/IntelliJProjects/SocksProxyBuilder/data/proxies/working-proxies-history-geo.csv"
        )

        val writer = Files.newBufferedWriter(outPath)

        val format = CSVFormat.Builder.create()
        format.setHeader("IP", "PORT", "PORTS", "PROTOCOL", "PROTOCOLS",
            "DETECTED", "TYPE", "RISK", "ASN", "PROVIDER", "CONTINENT",
            "COUNTRY", "ISOCODE", "REGION", "REGIONCODE", "CITY", "LATITUDE",
            "LONGITUDE")

        val csvPrinter = CSVPrinter(writer, format.build())

        for(proxyData in masterProxyData) {
            csvPrinter.printRecord(proxyData.ip, proxyData.port, "{${proxyData.ports?.joinToString(separator = ",")}}", proxyData.protocol,
                "{${proxyData.protocols.joinToString(separator = ",")}}", proxyData.detected, proxyData.type, proxyData.risk, proxyData.asn, proxyData.provider,
                proxyData.continent, proxyData.country, proxyData.isocode, proxyData.region, proxyData.regioncode, proxyData.city,
                proxyData.latitude, proxyData.longitude)
        }

        csvPrinter.flush()
        csvPrinter.close()
    }

    //Very inefficient, TODO - Update this
    private fun appendMasterDataArray(
        masterProxyDataArray: ArrayList<MasterProxyData>, protocol: String,
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
                        localProxy.longitude, toNumeric(ip))
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

    private fun runtime(startTime: Long, endTime: Long): String {
        val finalTime = endTime - startTime
        return String.format("%d min, %d sec",
            TimeUnit.MILLISECONDS.toMinutes(finalTime),
            TimeUnit.MILLISECONDS.toSeconds(finalTime) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(finalTime))
        )
    }

}