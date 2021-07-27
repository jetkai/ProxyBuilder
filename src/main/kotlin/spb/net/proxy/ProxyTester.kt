package spb.net.proxy

import spb.Constants
import spb.event.Event
import spb.net.rs.ClientSocket
import spb.net.rs.Stream
import spb.util.Config
import spb.util.FileBuilder
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
class ProxyTester : Event(5) {

    var proxyAddress = ""   // "116.233.137.127" {SOCKS4-EXAMPLE}
    var proxyPort = -1      // 4145 {SOCKS4-EXAMPLE}
    var type = ""
    var socks4 = false

    private var formattedProxy = ""
    private var connected = false
    private var started = false
    private var attempt = 0

    override fun run() {
        if(connected || attempt == 2) this.isRunning = false
        if(!started) init()
    }

    private fun init() {
        attempt++
        formattedProxy = "$proxyAddress:$proxyPort"
        if(proxyAddress.isNotEmpty() && proxyPort > 0)
            connectRS()
        else
            println("Proxy is empty.")
    }

    /**
     * Connects to a random RuneScape Private Server
     * Sends Bytes {14 & 0} - Init Login Packet
     * Returns responseCode 0 if successful, -1 if unsuccessful
     */
    private fun connectRS() {
        val serverAddress = if(attempt > 1) Config.values?.victimBackupServerIp else Config.values?.victimTestServerIp
        val serverPort = if(attempt > 1) Config.values?.victimBackupServerPort else Config.values?.victimTestServerPort

        val clientSocket = if (Constants.IS_USING_PROXY && type.contains("socks"))
            useSocksProxy(serverAddress, serverPort!!.toInt())
        else if(Constants.IS_USING_PROXY && type.contains("http"))
            useHttpProxy(serverAddress, serverPort!!.toInt())
        else ClientSocket().init(
            Socket(InetAddress.getByName(serverAddress), serverPort!!.toInt())
        )
        if(clientSocket == null) {
            println("Failed to connect to Proxy $formattedProxy")
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
            if(attempt == 1)
                init() //Restarts for second attempt on secondary test server
            println("Connected to Proxy successfully, but failed to connect to RSPS with Proxy $formattedProxy [${type.uppercase()}]")
        }
    }

    /**
     *
     */
    private fun useSocksProxy(serverAddress: String?, serverPort: Int): ClientSocket? {
        val proxy = Proxy(Proxy.Type.SOCKS, InetSocketAddress(proxyAddress, proxyPort))
        val socket = Socket(proxy)
        if(socks4)
            forceSocks4(socket)
        try {
            socket.soTimeout = 5000
            socket.tcpNoDelay = true
            socket.connect(InetSocketAddress(serverAddress, serverPort))
        } catch (e : IOException) {
            socket.close()
        }
        if(socket.isClosed) {
            if(attempt == 1)
                init()
            return null
        }
        return ClientSocket().init(socket)
    }

    private fun useHttpProxy(serverAddress: String?, serverPort: Int): ClientSocket? {
        val proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress(proxyAddress, proxyPort))
        val iNet : SocketAddress = proxy.address()
        val iNet2 = iNet as InetSocketAddress
        val socket = Socket(iNet2.hostName, iNet2.port)
        try {
            socket.soTimeout = 5000
            val outStream: OutputStream = socket.getOutputStream()
            outStream.write(("CONNECT $serverAddress:$serverPort HTTP/1.0\n\n").byteInputStream(StandardCharsets.ISO_8859_1).readBytes())
            outStream.flush()
            val inStream = BufferedReader(InputStreamReader(socket.getInputStream()))
            val httpConLine = inStream.readLine()
            if(Constants.DEBUG_MODE)
                println(httpConLine)
        } catch (e : IOException) {
            if(Constants.DEBUG_MODE)
                println(e.message)
            socket.close()
        }
        if(socket.isClosed) {
            if(attempt == 1)
                init()
            return null
        }
        return ClientSocket().init(socket)
    }

    /**
     * Requires JDK 1.8 {REFLECTION}
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
        FileBuilder.appendTxtFiles(formattedProxy, type)
        FileBuilder.appendJsonFiles(formattedProxy, type)
        println("Successfully connected to an RSPS with the Proxy $formattedProxy [${type.uppercase()}]")
    }

}