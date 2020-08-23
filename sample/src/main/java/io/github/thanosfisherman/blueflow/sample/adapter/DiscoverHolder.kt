package io.github.thanosfisherman.blueflow.sample.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import io.github.thanosfisherman.blueflow.BluetoothDeviceWrapper
import kotlinx.android.synthetic.main.item_scan.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class DiscoverHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bindItem(item: BluetoothDeviceWrapper) {

        itemView.deviceName.text = item.bluetoothDevice.name
        itemView.deviceMAC.text = item.bluetoothDevice.address
        itemView.deviceSignal.text = item.rssi.toString()
    }
}