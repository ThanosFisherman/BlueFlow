package io.github.thanosfisherman.blueflow

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

data class ServiceEvent(
    val state: State,
    val profileType: Int,
    val bluetoothProfile: BluetoothProfile? = null
) {

    enum class State {
        CONNECTED,
        DISCONNECTED;
    }

}

data class ConnectionStateEvent(
    val state: Int,
    val previousState: Int,
    val bluetoothDevice: BluetoothDevice?
)

data class BondStateEvent(
    val state: Int,
    val previousState: Int,
    val bluetoothDevice: BluetoothDevice?
)

data class AclEvent(val action: String, val bluetoothDevice: BluetoothDevice?)

@Parcelize
data class BluetoothDeviceWrapper(val bluetoothDevice: BluetoothDevice, val rssi: Int) : Parcelable