package io.github.thanosfisherman.blueflow.sample.usecase

import android.bluetooth.BluetoothDevice
import io.github.thanosfisherman.blueflow.BlueFlow
import io.github.thanosfisherman.blueflow.BluetoothDeviceWrapper
import io.github.thanosfisherman.blueflow.sample.BtDiscoveryState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

@ExperimentalCoroutinesApi
class DiscoverBtDevicesUseCase(private val blueFlow: BlueFlow) {
    private val deviceList = mutableListOf<BluetoothDeviceWrapper>()

    fun getBondedDevices(): List<BluetoothDevice>? {
        return blueFlow.bondedDevices()?.toList()
    }

    fun startDiscovery(): Boolean {
        if (!blueFlow.isDiscovering())
            return blueFlow.startDiscovery()
        return true
    }

    fun cancelDiscovery(): Boolean {
        if (blueFlow.isDiscovering())
            return blueFlow.cancelDiscovery()
        return true
    }

    fun discoverBondedDevices() = flow<BtDiscoveryState> {
        with(deviceList) {
            clear()
            getBondedDevices()?.forEach {
                add(BluetoothDeviceWrapper(it, 0))
                val deviceWrapperList =
                    this.distinctBy { devList -> devList.bluetoothDevice.address }
                emit(BtDiscoveryState.BtDiscoverySuccess(deviceWrapperList))
            }
        }
    }

    fun discoverDevices() = flow {
        deviceList.clear()
        try {
            blueFlow.discoverDevices().collect { device ->

                val deliveredDevices = with(deviceList) {
                    add(device)
                    distinctBy { it.bluetoothDevice.address }.map { it }
                }
                emit(BtDiscoveryState.BtDiscoverySuccess(deliveredDevices))
            }
        } catch (e: Exception) {
            emit(BtDiscoveryState.BtDiscoveryError)
        }
    }
}