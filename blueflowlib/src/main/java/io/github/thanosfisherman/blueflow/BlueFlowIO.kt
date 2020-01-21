package io.github.thanosfisherman.blueflow

import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class BlueFlowIO(val bluetoothSocket: BluetoothSocket) {

    var minExpectedBytes: Int = 2
    var buffer = ByteArray(1024) // buffer store for the stream

    private var isConnected = false
    private val inputStream: InputStream by lazy {
        try {
            isConnected = true
            bluetoothSocket.inputStream
        } catch (e: IOException) {
            isConnected = false
            throw SocketStreamException("Couldn't open InputStream socket")
        } finally {
            if (!isConnected)
                closeConnections()
        }
    }
    private val outputStream: OutputStream by lazy {
        try {
            isConnected = true
            bluetoothSocket.outputStream
        } catch (e: IOException) {
            isConnected = false
            throw SocketStreamException("Couldn't open OutputStream socket")
        } finally {
            if (!isConnected)
                closeConnections()
        }
    }

    /**
     * Send array of bytes to bluetooth output stream.
     *
     * @param bytes data to send
     * @return true if success, false if there was error occurred or disconnected
     */
    fun send(bytes: ByteArray): Boolean {
        if (!isConnected) return false
        return try {
            outputStream.write(bytes)
            outputStream.flush()
            true
        } catch (e: IOException) {
            isConnected = false
            false
        } finally {
            if (!isConnected)
                closeConnections()
        }
    }

    /**
     * Send one byte to bluetooth output stream.
     *
     * @param oneByte a byte
     * @return true if success, false if there was error occurred or disconnected
     */
    fun send(oneByte: Byte) = send(byteArrayOf(oneByte))

    /**
     * Send string of text to bluetooth output stream.
     *
     * @param text text to send
     * @return true if success, false if there was error occurred or disconnected
     */
    fun send(text: String) = send(text.toByteArray())

    /**
     * Observes byte from bluetooth's {@link InputStream}. Will be emitted per byte.
     *
     * @return Flow Observable with {@link Byte}
     */
    @ExperimentalCoroutinesApi
    fun readByteStream() = channelFlow {
        while (isActive) {
            try {
                offer(inputStream.read().toByte())
            } catch (e: IOException) {
                isConnected = false
                error("Couldn't read bytes from flow. Disconnected")
            } finally {
                if (!isConnected)
                    closeConnections()
            }
        }
    }.flowOn(Dispatchers.IO)

    @ExperimentalCoroutinesApi
    fun readByteStream(readInterceptor: (List<Byte>) -> Boolean) = channelFlow {

        while (isActive) {
            try {
                if (inputStream.available() < minExpectedBytes) {
                    delay(1000)
                    continue
                }
                val numBytes = inputStream.read(buffer)
                val bytesList = trimBuffer(buffer, numBytes)
                if (readInterceptor(bytesList)) {
                    bytesList.forEach { offer(it) }
                } else {
                    delay(1000)
                }
            } catch (e: IOException) {
                isConnected = false
                closeConnections()
                error("Couldn't read bytes from flow. Disconnected")
            } finally {
                if (!isConnected)
                    closeConnections()
            }
        }
    }.flowOn(Dispatchers.IO)

    private fun trimBuffer(buffer: ByteArray, numBytes: Int): List<Byte> {
        val data = ByteArray(numBytes)
        System.arraycopy(buffer, 0, data, 0, numBytes)
        return data.toList()
    }

    /**
     * Close the streams and socket connection.
     */
    fun closeConnections() {
        isConnected = false
        try {
            inputStream.close()
            outputStream.close()
            bluetoothSocket.close()
        } catch (e: Exception) {
        }
    }

}