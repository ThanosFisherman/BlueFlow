package io.github.thanosfisherman.blueflow.sample.viewmodel

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import io.github.thanosfisherman.blueflow.sample.BluetoothActionEnum
import io.github.thanosfisherman.blueflow.sample.BtConnection
import io.github.thanosfisherman.blueflow.sample.BtDiscoveryState
import io.github.thanosfisherman.blueflow.sample.BtNavigateState
import io.github.thanosfisherman.blueflow.sample.usecase.BtConnectUseCase
import io.github.thanosfisherman.blueflow.sample.usecase.BtNavigationUseCase
import io.github.thanosfisherman.blueflow.sample.usecase.DiscoverBtDevicesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.asFlow

@FlowPreview
@ExperimentalCoroutinesApi
class BluetoothViewModel(
    private val btNavigationUseCase: BtNavigationUseCase,
    private val btDiscoverUseCase: DiscoverBtDevicesUseCase,
    private val btConnectUseCase: BtConnectUseCase
) : ViewModel() {

    val btNavigationLive: LiveData<BtNavigateState> =
        btNavigationUseCase.btNavigateChannel.asFlow().asLiveData(viewModelScope.coroutineContext)


    fun initBtNavigation(bluetoothActionEnum: BluetoothActionEnum) {
        btNavigationUseCase.execBtNavigateFlow(bluetoothActionEnum)
    }

    fun discoverBtDevices(): LiveData<BtDiscoveryState> {
        btDiscoverUseCase.startDiscovery()
        return btDiscoverUseCase.discoverDevices().asLiveData(viewModelScope.coroutineContext)
    }

    fun getBondedDevices(): LiveData<BtDiscoveryState> {
        return btDiscoverUseCase.discoverBondedDevices().asLiveData()
    }

    fun cancelDiscovery() {
        btDiscoverUseCase.cancelDiscovery()
    }

    fun connect(device: BluetoothDevice): LiveData<BtConnection> {
        return btConnectUseCase.connect(device).asLiveData()
    }
}