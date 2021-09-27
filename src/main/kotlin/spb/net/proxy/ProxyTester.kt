package spb.net.proxy

import kotlinx.serialization.ExperimentalSerializationApi
import spb.Constants
import spb.event.Event
import spb.net.rs.ClientSocket
import spb.net.rs.Stream
import spb.util.Config
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.net.*
import java.nio.charset.StandardCharsets

/**
 * @author Kai
 */
@ExperimentalSerializationApi
class ProxyTester : Event(5) {

    private var formattedProxy = "" // 116.233.137.127:4145
    private var connected = false
    private var started = false
    private var attempt = 0         // Maximum 2

    var proxyAddress = ""   // "116.233.137.127" {SOCKS4-EXAMPLE}
    var proxyPort = -1      // 4145 {SOCKS4-EXAMPLE}
    var type = ""           // SOCKS4, SOCKS5, HTTP, HTTPS
    var socks4 = false

    override fun run() {
        if(connected || attempt >= 3) this.isRunning = false
        if(!started) init()
    }

    private fun init() {
        attempt++
        formattedProxy = "$proxyAddress:$proxyPort"
        if(proxyAddress.isNotEmpty() && proxyPort > 0)
            try {
                connectRS()
            } catch (e : Exception) {
                if(Constants.DEBUG_MODE && Constants.DISPLAY_CONNECTION_MESSAGE)
                    println(e.stackTraceToString())
            }
        else
            println("Proxy is empty.")
    }

    /**
     * Connects to a random RuneScape Private Server
     * Sends Bytes {14 & 0} - Init Login Packet
     * Returns responseCode 0 if successful, -1 if unsuccessful
     */
    private fun connectRS() {
        val serverAddress = Config.values?.victimTestServerIp?.get(attempt - 1)
        val serverPort = Config.values?.victimTestServerPort?.get(attempt - 1)
        val clientSocket = if (Constants.IS_USING_PROXY && type.contains("socks"))
            useSocksProxy(serverAddress, serverPort!!.toInt())
        else if(Constants.IS_USING_PROXY && type.contains("http"))
            useHttpProxy(serverAddress, serverPort!!.toInt())
        else ClientSocket().init(
            Socket(InetAddress.getByName(serverAddress), serverPort!!.toInt())
        )

        if(clientSocket == null) {
            if(Constants.DEBUG_MODE && Constants.DISPLAY_CONNECTION_MESSAGE)
                println("Failed to connect to Proxy $formattedProxy | Endpoint: $serverAddress:$serverPort | Attempt: $attempt")
            if(attempt < 3)
                init() //Restarts for second attempt on secondary test server
            return
        }

        val stream : Stream = Stream.create(2) //2 bytes only needed for init connection

        stream.writeByte(14)
        stream.writeByte(0)

        clientSocket.queueBytes(2, stream.buffer)
        for (inBytes in 0..7)
            clientSocket.read()

        //Expecting 0, -1 = failed to connect
        val responseCode : Int? = clientSocket.read()

        clientSocket.close()

        if(responseCode == 0)
            connected()
        else {
            if(Constants.DEBUG_MODE && Constants.DISPLAY_CONNECTION_MESSAGE)
                println("Connected to Proxy successfully, but failed to connect to RSPS ($serverAddress:$serverPort) with Proxy $formattedProxy [${type.uppercase()}]")
            if(attempt < 3)
                init() //Restarts for second attempt on secondary test server
        }
    }

    private fun useSocksProxy(serverAddress: String?, serverPort: Int): ClientSocket? {
        val proxy = Proxy(Proxy.Type.SOCKS, InetSocketAddress(proxyAddress, proxyPort))
        val socket = Socket(proxy)
        if(socks4)
            forceSocks4(socket)
        try {
            socket.soTimeout = 3000
            socket.tcpNoDelay = true
            socket.connect(InetSocketAddress(serverAddress, serverPort), 3000)
        } catch (e : IOException) {
            socket.close()
        }
        if(socket.isClosed)
            return null
        return ClientSocket().init(socket)
    }

    private fun useHttpProxy(serverAddress: String?, serverPort: Int): ClientSocket? {
        val proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress(proxyAddress, proxyPort))
        val iNet : SocketAddress = proxy.address()
        val iNet2 = iNet as InetSocketAddress
        val socket = Socket(iNet2.hostName, iNet2.port)
        try {
            socket.soTimeout = 3000
            val outStream: OutputStream = socket.getOutputStream()
            outStream.write(("CONNECT $serverAddress:$serverPort HTTP/1.0\n\n").byteInputStream(StandardCharsets.ISO_8859_1).readBytes())
            outStream.flush()
            if(Constants.DEBUG_MODE) {
                val inStream = BufferedReader(InputStreamReader(socket.getInputStream()))
                //Good Response = "HTTP/1.0 200 {OK/Connection established}" or "HTTP/1.1 200 {OK/Connection established}
                val response = inStream.readLine()
                if(Constants.DISPLAY_CONNECTION_MESSAGE)
                    println(response)
            }
        } catch (e : IOException) {
            //Bad Response = "Connection reset", "Read timed out", "null"
            if(Constants.DEBUG_MODE && Constants.DISPLAY_CONNECTION_MESSAGE)
                println(e.message)
            socket.close()
        }
        if(socket.isClosed)
            return null
        return ClientSocket().init(socket)
    }

    /**
     * Requires JDK 1.8 -> 15.02 {REFLECTION}
     */
    private fun forceSocks4(socket : Socket) {
        val setSockVersion : Method
        val sockImplField : Field = socket.javaClass.getDeclaredField("impl")
        sockImplField.isAccessible = true
        val socksImpl : SocketImpl = sockImplField.get(socket) as SocketImpl
        val clazzSocksImpl: Class<*> = socksImpl.javaClass

        setSockVersion = clazzSocksImpl.getDeclaredMethod("setV4")
        setSockVersion.isAccessible = true
        setSockVersion?.invoke(socksImpl)
        sockImplField.set(socket, socksImpl)
    }

    private fun connected() {
        this.connected = true
        this.isRunning = false
        when (type) {
            "socks4" -> VerifiedProxies.socks4 += formattedProxy
            "socks5" -> VerifiedProxies.socks5 += formattedProxy
            "http" -> VerifiedProxies.http += formattedProxy
            "https" -> VerifiedProxies.https += formattedProxy
        }
        when { Constants.DEBUG_MODE && Constants.DISPLAY_CONNECTION_MESSAGE ->
            println("Successfully connected to an RSPS with the Proxy $formattedProxy [${type.uppercase()}]")
        }
    }

}