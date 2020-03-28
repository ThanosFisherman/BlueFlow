package io.github.thanosfisherman.blueflow

import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class BlueFlowIO(val bluetoothSocket: BluetoothSocket?) {


    private val inputStream: InputStream? =
        try {
            bluetoothSocket?.inputStream
        } catch (e: IOException) {
            e.printStackTrace()
            throw SocketStreamException("Couldn't open InputStream socket")
        }

    private val outputStream: OutputStream? =
        try {
            bluetoothSocket?.outputStream
        } catch (e: IOException) {
            e.printStackTrace()
            throw SocketStreamException("Couldn't open OutputStream socket")
        }

    /**
     * Send array of bytes to bluetooth output stream.
     *
     * @param bytes data to send
     * @return true if success, false if there was error occurred or disconnected
     */
    fun send(bytes: ByteArray): Boolean {

        if (bluetoothSocket?.isConnected != true) return false

        return try {
            outputStream?.write(bytes)
            outputStream?.flush()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
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
        if (inputStream == null) {
            throw NullPointerException("inputStream is null. Perhaps bluetoothSocket is also null")
        }

        while (isActive) {
            try {
                offer(inputStream.read().toByte())
            } catch (e: IOException) {
                error("Couldn't read bytes from flow. Disconnected")
            }
        }
    }.flowOn(Dispatchers.IO)

    @ExperimentalCoroutinesApi
    fun readByteArrayStream(
        delayMillis: Long = 1000,
        minExpectedBytes: Int = 2,
        bufferCapacity: Int = 1024,
        readInterceptor: (ByteArray) -> ByteArray? = { it }
    ): Flow<ByteArray> = channelFlow {

        if (inputStream == null) {
            throw NullPointerException("inputStream is null. Perhaps bluetoothSocket is also null")
        }
        val buffer = ByteArray(bufferCapacity)
        val byteAccumulatorList = mutableListOf<Byte>()
        while (isActive) {
            try {
                if (inputStream.available() < minExpectedBytes) {
                    delay(delayMillis)
                    continue
                }
                val numBytes = inputStream.read(buffer)
                val readBytes = buffer.trim(numBytes)
                if (byteAccumulatorList.size >= bufferCapacity)
                    byteAccumulatorList.clear()

                byteAccumulatorList.addAll(readBytes.toList())
                val interceptor = readInterceptor(byteAccumulatorList.toByteArray())

                if (interceptor == null)
                    delay(delayMillis)

                interceptor?.let {
                    offer(it)
                    byteAccumulatorList.clear()
                }

            } catch (e: IOException) {
                byteAccumulatorList.clear()
                closeConnections()
                error("Couldn't read bytes from flow. Disconnected")
            } finally {
                if (bluetoothSocket?.isConnected != true) {
                    byteAccumulatorList.clear()
                    closeConnections()
                    break
                }
            }
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Close the streams and socket connection.
     */
    fun closeConnections() {
        try {
            inputStream?.close()
            outputStream?.close()
            bluetoothSocket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isConnected() = bluetoothSocket?.isConnected ?: false
}