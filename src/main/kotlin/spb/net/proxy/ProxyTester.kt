package spb.net.proxy

import spb.Constants
import spb.net.rs.ClientSocket
import spb.net.rs.Stream
import java.io.IOException
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.net.*
import kotlin.system.exitProcess

class ProxyTester {

    val proxyAddress = ""   // "116.233.137.127" {SOCKS4-EXAMPLE}
    val proxyPort = -1      // 4145 {SOCKS4-EXAMPLE}

    fun init() {
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

        val stream: Stream = Stream.create(2) //2 bytes only needed for init connection

        val clientSocket = when {
            Constants.IS_USING_PROXY -> useProxy(Constants.VICTIM_TEST_SERVER_IP, Constants.VICTIM_TEST_SERVER_PORT)
            else -> ClientSocket().init(
                Socket(InetAddress.getByName(Constants.VICTIM_TEST_SERVER_IP), Constants.VICTIM_TEST_SERVER_PORT)
            )
        }

        stream.writeByte(14)
        stream.writeByte(0)

        clientSocket.queueBytes(2, stream.buffer)
        for (inBytes in 0..7)
            clientSocket.read()

        val responseCode : Int = clientSocket.read()

        clientSocket.close()
        println("Response Code: $responseCode")
    }

    /**
     *
     */
    private fun useProxy(serverAddress: String?, serverPort: Int): ClientSocket {

        val proxy = Proxy(Proxy.Type.SOCKS, InetSocketAddress(proxyAddress, proxyPort))
        val socket = Socket(proxy)
        forceSocks4(socket)
        try {
            socket.soTimeout = 10000
            socket.connect(InetSocketAddress(serverAddress, serverPort))
        } catch (e: IOException) {
            e.printStackTrace()
            socket.close()
        }
        if(socket.isClosed) {
            println("Failed to connect to Proxy. Closing App")
            exitProcess(0)
        }
        return ClientSocket().init(socket)
    }

    private fun forceSocks4(socket : Socket) {
        val setSockVersion : Method
        val sockImplField : Field = socket.javaClass.getDeclaredField("impl")
        sockImplField.isAccessible = true
        val socksImpl: SocketImpl = sockImplField.get(socket) as SocketImpl
        val clazzSocksImpl: Class<*> = socksImpl.javaClass

        setSockVersion = clazzSocksImpl.getDeclaredMethod("setV4")
        setSockVersion.isAccessible = true
        setSockVersion?.invoke(socksImpl)
        sockImplField.set(socket, socksImpl)
    }

}