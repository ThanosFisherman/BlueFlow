package io.github.thanosfisherman.blueflow

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import kotlinx.coroutines.*
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

fun BluetoothSocket.readByteStream(readInterceptor: (ByteArray) -> Boolean): Nothing = TODO()