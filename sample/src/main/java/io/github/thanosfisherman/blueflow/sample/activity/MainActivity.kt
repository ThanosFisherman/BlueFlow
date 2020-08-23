package io.github.thanosfisherman.blueflow.sample.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.thanosfisherman.blueflow.sample.BluetoothActionEnum
import io.github.thanosfisherman.blueflow.sample.BtConnection
import io.github.thanosfisherman.blueflow.sample.BtDiscoveryState
import io.github.thanosfisherman.blueflow.sample.R
import io.github.thanosfisherman.blueflow.sample.adapter.DiscoverRecyclerAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

private const val REQUEST_PERMISSION_COARSE_LOCATION = 0

@FlowPreview
@ExperimentalCoroutinesApi
class MainActivity : BaseActivity() {

    override val layoutResId: Int = R.layout.activity_main

    private var adapter: DiscoverRecyclerAdapter? = null

    override fun executeBtCommand(params: BluetoothActionEnum) {
        when (params) {
            BluetoothActionEnum.DISCOVER_DEVICES -> {
                viewModel.discoverBtDevices().observe(this) { observeDeviceDiscoveryState(it) }
            }
            BluetoothActionEnum.GET_BONDED_DEVICES -> {
                viewModel.getBondedDevices().observe(this) { observeDeviceDiscoveryState(it) }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter = DiscoverRecyclerAdapter()
        adapter?.setOnItemClickListener {
            viewModel.connect(it.bluetoothDevice).observe(this) { checkBtConnectionState(it) }
        }
        val layoutManager = LinearLayoutManager(applicationContext)
        recycler_scan.setHasFixedSize(true)
        recycler_scan.layoutManager = layoutManager
        recycler_scan.adapter = adapter

        btnBounded.setOnClickListener {
            viewModel.initBtNavigation(BluetoothActionEnum.GET_BONDED_DEVICES)
        }

        btnStart.setOnClickListener {

            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    REQUEST_PERMISSION_COARSE_LOCATION
                )
            } else {
                viewModel.initBtNavigation(BluetoothActionEnum.DISCOVER_DEVICES)
            }
        }
    }

    private fun observeDeviceDiscoveryState(btDiscoveryState: BtDiscoveryState) {
        when (btDiscoveryState) {
            is BtDiscoveryState.BtDiscoverySuccess -> {
                adapter?.submitList(btDiscoveryState.devices)
            }

            is BtDiscoveryState.BtDiscoveryError -> {
                Toast.makeText(applicationContext, "SOMETHING WENT WRONG", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun checkBtConnectionState(btConnection: BtConnection) {
        when (btConnection) {
            is BtConnection.BtConnectingLoadingState -> {
                Toast.makeText(applicationContext, "CONNECTING PLEASE WAIT...", Toast.LENGTH_LONG).show()
            }
            is BtConnection.BtConnectedState -> {
                Toast.makeText(applicationContext, "CONNECTED", Toast.LENGTH_LONG).show()
            }
            is BtConnection.BtErrorConnectingState -> {
                Toast.makeText(applicationContext, "SOMETHING WENT WRONG WHILE CONNECTING", Toast.LENGTH_LONG).show()
            }
            is BtConnection.BtDisconnectedState -> {
                Toast.makeText(applicationContext, "DISCONNECTED", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
