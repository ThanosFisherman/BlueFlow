package io.github.thanosfisherman.blueflow.sample.common

import android.os.Build

fun isAndroidQAndAbove(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        return true
    return false
}
