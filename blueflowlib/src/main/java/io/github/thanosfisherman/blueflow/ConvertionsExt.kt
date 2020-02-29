package io.github.thanosfisherman.blueflow

import java.math.BigInteger
import java.util.*
import kotlin.experimental.and
import kotlin.math.pow

val String.toByteArrayFromHex
    inline get() = this.chunked(2).map {
        it.toUpperCase(Locale.ENGLISH).toInt(16).toByte()
    }.toByteArray()
val ByteArray.toHexLower
    inline get() = this.joinToString(separator = "") {
        String.format(
            "%02x",
            (it.toInt() and 0xFF)
        )
    }
val ByteArray.toHexUpper
    inline get() = this.joinToString(separator = "") {
        String.format(
            "%02X",
            (it.toInt() and 0xFF)
        )
    }
val ByteArray.toHexString inline get() = this.toHexUpper

fun Byte.toHex() = String.format("%02X", this)

fun ByteArray.trim(size: Int): ByteArray {
    val data = ByteArray(size)
    System.arraycopy(this, 0, data, 0, size)
    return data
}

fun String.incLittleEndian(step: Int = 1): String {
    val reversedLong = this.chunked(2).reversed().joinToString("").hexToDecLong().plus(step)
    var reversedHex = reversedLong.toHex
    while (reversedHex.length < this.length)
        reversedHex = "0$reversedHex"
    return reversedHex.chunked(2).reversed().joinToString("").toUpperCase(Locale.ENGLISH)
}

fun String.reverseLittleEndian(): String {
    return this.chunked(2).reversed().joinToString("")
}

fun ByteArray.getHexAtIndex(index: Int): String {

    var byte: Byte? = null

    try {
        byte = this[index]
    } catch (exception: ArrayIndexOutOfBoundsException) {
        exception.printStackTrace()
    }

    return String.format("%02X", byte)
}

fun String.binToDec(): Int {
    var num = this.toLong()
    var decimalNumber = 0
    var i = 0
    var remainder: Long

    while (num.toInt() != 0) {
        remainder = num % 10
        num /= 10
        decimalNumber += (remainder * 2.0.pow(i.toDouble())).toInt()
        ++i
    }
    return decimalNumber
}


fun String.hexToBin(): String {
    var preBin = BigInteger(this, 16).toString(2)

    if (preBin.length < this.length * 4) {
        for (i in 0 until this.length * 4 - preBin.length) {
            preBin = "0$preBin"
        }
    }

    if (preBin.length % 2 != 0) {
        preBin = "0$preBin"
    }

    return preBin
}


fun String.hexToDecLong(): Long {

    return this.toLong(16)

}

fun String.hexToDecInt(): Int {

    return this.toInt(16)

}

fun Byte.byteToBin(): String {

    var result = ""
    var byte = this

    var counter: Int = Byte.SIZE_BITS
    val mask = (0b10000000).toByte()

    while (counter > 0) {
        val c = if (byte.and(mask) == mask) '1' else '0'
        result += c

        byte = byte.toInt().shl(1).toByte()
        counter -= 1
    }
    return result

}

val Int.toHex: String inline get() = Integer.toHexString(this)
val Long.toHex: String inline get() = this.toString(16)

fun Int.toHexLower(length: Int): String {
    val hexString = Integer.toHexString(this)
    val builder = StringBuilder().append(String.format("%0${length}d", 0))
    return builder.replace(length - hexString.length, length, hexString).toString()
}

fun Int.toHexUpper(length: Int): String {
    return toHexLower(length).toUpperCase(Locale.ENGLISH)
}

fun Long.toHexLower(length: Int): String {
    val hexString = this.toString(16)
    val builder = StringBuilder().append(String.format("%0${length}d", 0))
    return builder.replace(length - hexString.length, length, hexString).toString()
}

fun Long.toHexUpper(length: Int): String {
    return toHexLower(length).toUpperCase(Locale.ENGLISH)
}