package io.github.thanosfisherman.blueflow.sample.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import io.github.thanosfisherman.blueflow.BluetoothDeviceWrapper
import io.github.thanosfisherman.blueflow.sample.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class DiscoverRecyclerAdapter : ListAdapter<BluetoothDeviceWrapper, DiscoverHolder>(DeviceDiffCallback()) {

    var itemclicks: ((BluetoothDeviceWrapper) -> Unit)? = null

    fun setOnItemClickListener(itemclicks: (BluetoothDeviceWrapper) -> Unit) {
        this.itemclicks = itemclicks
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscoverHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_scan, parent, false)
        return DiscoverHolder(v).apply {
            itemView.setOnClickListener { itemclicks?.invoke(getItem(adapterPosition)) }
        }
    }

    override fun onBindViewHolder(holder: DiscoverHolder, position: Int) {
        holder.bindItem(getItem(position))
    }

    private class DeviceDiffCallback : DiffUtil.ItemCallback<BluetoothDeviceWrapper>() {
        override fun areItemsTheSame(oldItem: BluetoothDeviceWrapper, newItem: BluetoothDeviceWrapper): Boolean {
            return oldItem.bluetoothDevice.address == newItem.bluetoothDevice.address
        }

        override fun areContentsTheSame(oldItem: BluetoothDeviceWrapper, newItem: BluetoothDeviceWrapper): Boolean {
            return oldItem.bluetoothDevice == newItem.bluetoothDevice
        }
    }
}