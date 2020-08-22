package io.github.thanosfisherman.blueflow.sample

import io.github.thanosfisherman.blueflow.BluetoothDeviceWrapper

sealed class BtNavigateState {

    object BtNotAvailableNavigateState : BtNavigateState()
    object BtAlreadyEnabledNavigateState : BtNavigateState()
    data class BtShowNativeDialogNavigateState(val callback: BtNativeDialogCallback) : BtNavigateState()
    object BtEnableSuccessNavigateState : BtNavigateState()
    object BtIdleNavigateState : BtNavigateState()
    data class BtConnectedExecuteState<out Type : Any>(val params: Type? = null) : BtNavigateState()
}

sealed class BtDiscoveryState {
    data class BtDiscoverySuccess(val devices: List<BluetoothDeviceWrapper>) : BtDiscoveryState()
    object BtDiscoveryError : BtDiscoveryState()
}

interface BtNativeDialogCallback {
    fun yes()
    fun no()
}