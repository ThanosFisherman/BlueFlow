package io.github.thanosfisherman.blueflow.sample

import android.util.Log
import io.github.thanosfisherman.blueflow.toHex

fun intercept(byteList: List<Byte>): Boolean {
    if (byteList.size == 2 && byteList[0] == 0x31.toByte() && byteList[1] == 0x32.toByte()) {
        return true
    } else {
        var packetLength = 0
        if (byteList[0] == 0x02.toByte() && byteList[1] == 0xFF.toByte()) {
            packetLength = byteList[2].toHex().toInt(16)
            Log.i("Interceptor","GOT PACKET LENGTH $packetLength")
        }

        if (packetLength == byteList.size - 4) {
            Log.i("Interceptor","verified! length Equals numBytes $packetLength, ${byteList.size}")
            return true
        }
    }
    return false
}