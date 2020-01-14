package io.github.thanosfisherman.blueflow

import android.bluetooth.BluetoothProfile

data class ServiceEvent(
    val state: State,
    val profileType: Int,
    val bluetoothProfile: BluetoothProfile
) {

    enum class State {
        CONNECTED,
        DISCONNECTED;
    }

}