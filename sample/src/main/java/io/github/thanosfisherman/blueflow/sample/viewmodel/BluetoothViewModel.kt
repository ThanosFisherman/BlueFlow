package io.github.thanosfisherman.blueflow.sample.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import io.github.thanosfisherman.blueflow.sample.BtDiscoveryState
import io.github.thanosfisherman.blueflow.sample.BtNavigateState
import io.github.thanosfisherman.blueflow.sample.usecase.BtNavigationUseCase
import io.github.thanosfisherman.blueflow.sample.usecase.DiscoverBtDevicesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.asFlow

@FlowPreview
@ExperimentalCoroutinesApi
class BluetoothViewModel(
    private val btNavigationUseCase: BtNavigationUseCase,
    private val btDiscoverUseCase: DiscoverBtDevicesUseCase
) : ViewModel() {

    private var job: Job? = null

    val btNavigationLive: LiveData<BtNavigateState> =
        btNavigationUseCase.btNavigateChannel.asFlow().asLiveData()


    fun initBtNavigation() {
        btNavigationUseCase.execBtNavigateFlow()
    }

    fun discoverBtDevices(): LiveData<BtDiscoveryState> {
        btDiscoverUseCase.startDiscovery()
        return btDiscoverUseCase.discoverDevices().asLiveData(viewModelScope.coroutineContext, 3000)
    }

    fun cancelDiscovery() {
        btDiscoverUseCase.cancelDiscovery()
    }
}