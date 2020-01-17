package io.github.thanosfisherman.blueflow

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket

fun BluetoothDevice.createRfcommSocket(channel: Int): BluetoothSocket {
    try {
        val method = this.javaClass.getMethod("createRfcommSocket", Integer.TYPE)
        return method.invoke(this, channel) as BluetoothSocket
    } catch (e: Exception) {
        throw UnsupportedOperationException(e)
    }
}