package io.github.thanosfisherman.blueflow.sample.usecase

import io.github.thanosfisherman.blueflow.BlueFlow
import io.github.thanosfisherman.blueflow.sample.BluetoothActionEnum
import io.github.thanosfisherman.blueflow.sample.BtNativeDialogCallback
import io.github.thanosfisherman.blueflow.sample.BtNavigateState
import io.github.thanosfisherman.blueflow.sample.common.EventWrapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel


@ExperimentalCoroutinesApi
class BtNavigationUseCase(private val blueFlow: BlueFlow) {

    val btNavigateChannel: BroadcastChannel<EventWrapper<BtNavigateState>> = ConflatedBroadcastChannel()

    fun execBtNavigateFlow(bluetoothActionEnum: BluetoothActionEnum) {

        if (!blueFlow.isBluetoothAvailable()) {
            btNavigateChannel.offer(EventWrapper(BtNavigateState.BtNotAvailableNavigateState))
            return
        }

        if (!blueFlow.isBluetoothEnabled()) {

            val listener = object : BtNativeDialogCallback {
                override fun yes() {
                    btNavigateChannel.offer(
                        EventWrapper(BtNavigateState.BtEnableSuccessNavigateState(bluetoothActionEnum))
                    )
                }

                override fun no() {

                }
            }
            btNavigateChannel.offer(
                EventWrapper(BtNavigateState.BtShowNativeDialogNavigateState(listener))
            )
        } else {
            btNavigateChannel.offer(EventWrapper(BtNavigateState.BtEnableSuccessNavigateState(bluetoothActionEnum)))
        }
    }
}
