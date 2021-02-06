package io.github.thanosfisherman.blueflow.sample.viewmodel

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.*
import io.github.thanosfisherman.blueflow.cancelIfActive
import io.github.thanosfisherman.blueflow.sample.BluetoothActionEnum
import io.github.thanosfisherman.blueflow.sample.BtConnection
import io.github.thanosfisherman.blueflow.sample.BtDiscoveryState
import io.github.thanosfisherman.blueflow.sample.BtNavigateState
import io.github.thanosfisherman.blueflow.sample.common.EventWrapper
import io.github.thanosfisherman.blueflow.sample.usecase.BtConnectUseCase
import io.github.thanosfisherman.blueflow.sample.usecase.BtNavigationUseCase
import io.github.thanosfisherman.blueflow.sample.usecase.DiscoverBtDevicesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@FlowPreview
@ExperimentalCoroutinesApi
class BluetoothViewModel(
    private val btNavigationUseCase: BtNavigationUseCase,
    private val btDiscoverUseCase: DiscoverBtDevicesUseCase,
    private val btConnectUseCase: BtConnectUseCase
) : ViewModel() {

    private var job: Job? = null
    val btNavigationLive: LiveData<EventWrapper<BtNavigateState>> =
        btNavigationUseCase.btNavigateChannel.asFlow().asLiveData()
    private val discoverLive = MutableLiveData<BtDiscoveryState>()

    fun initBtNavigation(bluetoothActionEnum: BluetoothActionEnum) {
        btNavigationUseCase.execBtNavigateFlow(bluetoothActionEnum)
    }

    fun discoverBtDevices(): LiveData<BtDiscoveryState> {
        job?.cancelIfActive()
        job = btDiscoverUseCase.discoverDevices().onEach { discoverLive.value = it }.launchIn(viewModelScope)
        return discoverLive
    }

    fun getBondedDevices(): LiveData<BtDiscoveryState> {
        job?.cancelIfActive()
        job = btDiscoverUseCase.discoverBondedDevices().onEach { discoverLive.value = it }.launchIn(viewModelScope)
        return discoverLive
    }

    fun startDiscovery() {
        btDiscoverUseCase.startDiscovery()
    }

    fun cancelDiscovery() {
        btDiscoverUseCase.cancelDiscovery()
    }

    fun connect(device: BluetoothDevice): LiveData<BtConnection> {
        return btConnectUseCase.connect(device).asLiveData()
    }
}