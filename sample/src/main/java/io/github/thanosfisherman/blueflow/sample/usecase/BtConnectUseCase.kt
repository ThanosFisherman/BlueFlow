package io.github.thanosfisherman.blueflow.sample.usecase

import android.bluetooth.BluetoothDevice
import io.github.thanosfisherman.blueflow.BlueFlow
import io.github.thanosfisherman.blueflow.sample.BtConnection
import kotlinx.coroutines.flow.flow
import java.util.*

class BtConnectUseCase(private val blueFlow: BlueFlow) {

    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

    fun connect(device: BluetoothDevice) = flow {
        emit(BtConnection.BtConnectingLoadingState(device))
        try {
            val socket = blueFlow.connectAsClientAsync(device, 2).await()
            emit(BtConnection.BtConnectedState(socket = socket))
        } catch (e: Exception) {
            emit(BtConnection.BtErrorConnectingState)
        }
    }
}