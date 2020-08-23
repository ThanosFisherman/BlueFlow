package io.github.thanosfisherman.blueflow.sample.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.thanosfisherman.blueflow.sample.usecase.BtConnectUseCase
import io.github.thanosfisherman.blueflow.sample.usecase.BtNavigationUseCase
import io.github.thanosfisherman.blueflow.sample.usecase.DiscoverBtDevicesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class ViewModelFactory(
    private val btNavigationUseCase: BtNavigationUseCase,
    private val btDiscoverUseCase: DiscoverBtDevicesUseCase,
    private val btConnectUseCase: BtConnectUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BluetoothViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BluetoothViewModel(btNavigationUseCase, btDiscoverUseCase, btConnectUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}