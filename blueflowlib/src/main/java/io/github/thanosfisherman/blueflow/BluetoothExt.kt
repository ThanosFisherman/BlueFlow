package io.github.thanosfisherman.blueflow

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import java.io.IOException
import java.util.*

fun BluetoothDevice.createRfcommSocket(channel: Int): BluetoothSocket {
    try {
        val method = this.javaClass.getMethod("createRfcommSocket", Integer.TYPE)
        return method.invoke(this, channel) as BluetoothSocket
    } catch (e: Exception) {
        throw UnsupportedOperationException(e)
    }
}

fun BluetoothDevice.isConnected(): Boolean {
    try {
        val method = this.javaClass.getMethod("isConnected")
        return method.invoke(this) as Boolean
    } catch (e: Exception) {
        throw UnsupportedOperationException(e)
    }
}

@ExperimentalCoroutinesApi
fun BluetoothAdapter.discoverDevices(context: Context) =
    BlueFlow.getInstance(context).discoverDevices()

suspend fun BluetoothDevice.connectAsClientAsync(uuid: UUID, secure: Boolean = true) =
    coroutineScope {
        return@coroutineScope async(Dispatchers.IO) {
            val bluetoothSocket =
                if (secure) createRfcommSocketToServiceRecord(uuid)
                else createInsecureRfcommSocketToServiceRecord(uuid)
            bluetoothSocket.also { it.connect() }
        }
    }

@ExperimentalCoroutinesApi
fun BluetoothSocket.readByteStream() = channelFlow {
    while (isActive) {
        try {
            offer(inputStream.read().toByte())
        } catch (e: IOException) {
            error("Couldn't read bytes from flow. Disconnected")
        }
    }
}.flowOn(Dispatchers.IO)

@ExperimentalCoroutinesApi
fun BluetoothSocket.readByteArrayStream(
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
                delay(1000)
                continue
            }
            val numBytes = inputStream.read(buffer)
            val readBytes = buffer.trim(numBytes)
            if (byteAccumulatorList.size >= bufferCapacity)
                byteAccumulatorList.clear()

            byteAccumulatorList.addAll(readBytes.toList())
            val interceptor = readInterceptor(byteAccumulatorList.toByteArray())

            if (interceptor == null)
                delay(1000)

            interceptor?.let {
                offer(it)
                byteAccumulatorList.clear()
            }
        } catch (e: IOException) {
            byteAccumulatorList.clear()
            close()
            error("Couldn't read bytes from flow. Disconnected")
        }
    }
}.flowOn(Dispatchers.IO)

fun BluetoothSocket.send(bytes: ByteArray): Boolean {

    if (!isConnected) return false

    return try {
        outputStream.write(bytes)
        outputStream.flush()
        true
    } catch (e: IOException) {
        e.printStackTrace()
        false
    }
}