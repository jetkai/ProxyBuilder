package spb.net.proxy

import spb.Constants
import spb.event.Event
import spb.net.rs.ClientSocket
import spb.net.rs.Stream
import java.io.IOException
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.net.*

/**
 * @author Kai
 */
class ProxyTester : Event(5) { //Run every 5 seconds

    var proxyAddress = ""   // "116.233.137.127" {SOCKS4-EXAMPLE}
    var proxyPort = -1      // 4145 {SOCKS4-EXAMPLE}
    var socks4 = false

    private var formattedProxy = "$proxyAddress:$proxyPort"
    private var connected = false
    private var started = false
    private var count = 0

    override fun run() {
        if(connected || count == 2) this.isRunning = false
        if(!started) init()
        count++
    }

    private fun init() {
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
        val clientSocket = when {
            Constants.IS_USING_PROXY -> useProxy(Constants.VICTIM_TEST_SERVER_IP, Constants.VICTIM_TEST_SERVER_PORT)
            else -> ClientSocket().init(
                Socket(InetAddress.getByName(Constants.VICTIM_TEST_SERVER_IP), Constants.VICTIM_TEST_SERVER_PORT)
            )
        }
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
        else
            println("Connected to Proxy successfully, but failed to connect to RSPS with Proxy $formattedProxy")
    }

    /**
     *
     */
    private fun useProxy(serverAddress: String?, serverPort: Int): ClientSocket? {

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
        if(socket.isClosed)
            return null
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
        if(socks4)
            ProxyGrabber.verifiedSocks4 += (formattedProxy)
        else
            ProxyGrabber.verifiedSocks5 += (formattedProxy)
        this.connected = true
        this.isRunning = false
        println("Successfully connected to an RSPS with the Proxy $formattedProxy")
    }

}