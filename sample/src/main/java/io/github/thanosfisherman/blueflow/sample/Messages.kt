package io.github.thanosfisherman.blueflow.sample

import android.util.Log
import io.github.thanosfisherman.blueflow.toHex

fun intercept(bytes: ByteArray): Boolean {

    if (bytes.size == 2 && bytes[0] == 0x31.toByte() && bytes[1] == 0x33.toByte()) {
        Log.i("Interceptor", "OK")
        return true
    } else {
        var packetLength = 0
        if (bytes[0] == 0x02.toByte() && bytes[1] == 0xFF.toByte()) {
            packetLength = bytes[2].toHex().toInt(16)
            Log.i("Interceptor", "GOT PACKET LENGTH $packetLength")
        }

        if (packetLength == bytes.size - 4) {
            Log.i("Interceptor", "verified! length Equals numBytes $packetLength, ${bytes.size}")
            return true
        }
    }
    return false
}

const val izarConf = "FEFEFE424C5545313133FF"
val UUID = java.util.UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")