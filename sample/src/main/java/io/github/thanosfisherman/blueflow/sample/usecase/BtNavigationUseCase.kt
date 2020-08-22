package io.github.thanosfisherman.blueflow.sample.usecase

import io.github.thanosfisherman.blueflow.BlueFlow
import io.github.thanosfisherman.blueflow.sample.BtNativeDialogCallback
import io.github.thanosfisherman.blueflow.sample.BtNavigateState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel


@ExperimentalCoroutinesApi
class BtNavigationUseCase(private val blueFlow: BlueFlow) {

    val btNavigateChannel: BroadcastChannel<BtNavigateState> = ConflatedBroadcastChannel()

    fun execBtNavigateFlow(params: Any? = null) {

        if (!blueFlow.isBluetoothAvailable()) {
            btNavigateChannel.offer(BtNavigateState.BtNotAvailableNavigateState)
            return
        }

        if (!blueFlow.isBluetoothEnabled()) {

            val listener = object : BtNativeDialogCallback {
                override fun yes() {
                    btNavigateChannel.offer(
                        BtNavigateState.BtEnableSuccessNavigateState
                    )
                }

                override fun no() {

                }
            }
            btNavigateChannel.offer(
                BtNavigateState.BtShowNativeDialogNavigateState(listener)
            )
        } else {
            btNavigateChannel.offer(BtNavigateState.BtConnectedExecuteState(params))
        }
    }
}
