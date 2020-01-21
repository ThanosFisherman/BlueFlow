package io.github.thanosfisherman.blueflow.sample

import android.util.Log
import io.github.thanosfisherman.blueflow.toHex
import io.github.thanosfisherman.blueflow.toHexString

fun intercept(bytes: ByteArray): Boolean {
    Log.i("Interceptor", bytes.toHexString)
    if (bytes.size == 2 && bytes[0] == 0x31.toByte() && bytes[1] == 0x32.toByte()) {
        Log.i("Interceptor", "IZAR CONFIGURED")
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
const val installMsg =
    "1003E61E7856341210C0000004000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
val UUID = java.util.UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")