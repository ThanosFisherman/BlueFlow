package io.github.thanosfisherman.blueflow

import android.bluetooth.BluetoothAdapter

/**
 * Thrown when [BluetoothAdapter.getProfileProxy] returns true, which means that connection
 * to bluetooth profile failed.
 */
class ProfileProxyException : RuntimeException("Failed to get profile proxy")
