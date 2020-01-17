package io.github.thanosfisherman.blueflow.sample

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.github.thanosfisherman.blueflow.BlueFlow
import io.github.thanosfisherman.blueflow.safeCollect
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*

private const val REQUEST_PERMISSION_COARSE_LOCATION = 0
private const val REQUEST_ENABLE_BT = 1

class MainActivity : AppCompatActivity() {

    private val devices = arrayListOf<BluetoothDevice>()
    private lateinit var blueFlow: BlueFlow

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        blueFlow = BlueFlow(applicationContext)
        start.setOnClickListener {
            devices.clear()
            setAdapter(devices)

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
                blueFlow.startDiscovery()
                var job = observeDevices()
                if (job.isActive) {
                    job.cancel()
                    job = observeDevices()
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    fun observeDevices(): Job {

        val uiScope = CoroutineScope(Dispatchers.Main)

        return uiScope.launch {
            blueFlow.discoverDevices().safeCollect {
                addDevice(it)
                Log.i("MAIN", it.address)
            }
        }
    }

    private fun addDevice(device: BluetoothDevice) {
        devices.add(device)
        setAdapter(devices)
    }

    private fun setAdapter(list: List<BluetoothDevice>) {
        val itemLayoutId = android.R.layout.simple_list_item_1
        result.adapter = object : ArrayAdapter<BluetoothDevice?>(
            this, android.R.layout.simple_list_item_2,
            android.R.id.text1, list
        ) {
            override fun getView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getView(position, convertView, parent)
                val device: BluetoothDevice = devices[position]
                var devName = device.name
                val devAddress = device.address
                if (TextUtils.isEmpty(devName)) {
                    devName = "NO NAME"
                }
                val text1 =
                    view.findViewById(android.R.id.text1) as TextView
                val text2 =
                    view.findViewById(android.R.id.text2) as TextView
                text1.text = devName
                text2.text = devAddress
                return view
            }
        }
    }
}
