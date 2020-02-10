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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.github.thanosfisherman.blueflow.BlueFlow
import io.github.thanosfisherman.blueflow.BluetoothDeviceWrapper
import io.github.thanosfisherman.blueflow.safeCollect
import io.github.thanosfisherman.blueflow.toHex
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.collect
import java.util.*

private const val REQUEST_PERMISSION_COARSE_LOCATION = 0
private const val REQUEST_ENABLE_BT = 1

@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity() {

    private var jobs = mutableListOf<CompletableJob?>()
    private val devices = arrayListOf<BluetoothDeviceWrapper>()
    private lateinit var blueFlow: BlueFlow

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        blueFlow = BlueFlow.getInstance(applicationContext)
        btnSend.setOnClickListener {
            Toast.makeText(
                applicationContext,
                "Under Construction",
                Toast.LENGTH_LONG
            ).show()
        }
        btnStart.setOnClickListener {
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
                if (blueFlow.isBluetoothAvailable()) { //Checks if bluetooth is supported on this device
                    if (blueFlow.isBluetoothEnabled()) { //Checks if bluetooth is actually turned ON for this device
                        observeDevices()
                        blueFlow.startDiscovery()
                    } else {
                        Toast.makeText(applicationContext, "PLEASE ENABLE BLUETOOTH", Toast.LENGTH_LONG).show()
                    }
                } else { //Bluetooth is NOT Supported on the device
                    Toast.makeText(applicationContext, "BLUETOOTH NOT AVAILABLE ON THIS DEVICE", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    private fun observeDevices() {

        val job = Job()
        jobs.add(job)
        CoroutineScope(IO + job).launch {

            blueFlow.discoverDevices().safeCollect {
                addDevice(it)
                Log.i("MAIN", it.bluetoothDevice.address)
            }
        }
    }

    private fun connect(device: BluetoothDevice, uuid: UUID) {

        jobs.forEach { it?.cancel() }
        val job = Job()
        jobs.add(job)

        CoroutineScope(IO + job).launch {
            val btSocket = blueFlow.connectAsClientAsync(device, uuid)
            val btio = blueFlow.getIO(btSocket.await())
            btio.readByteStream().collect {
                Log.i("Main", "Collected ${it.toHex()}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        jobs.forEach { it?.cancel() }
        if (blueFlow.isBluetoothAvailable())
            blueFlow.cancelDiscovery()
    }

    private suspend fun addDevice(device: BluetoothDeviceWrapper) = withContext(Main) {
        devices.add(device)
        setAdapter(devices)
    }

    private fun setAdapter(list: List<BluetoothDeviceWrapper>) {
        //val itemLayoutId = android.R.layout.simple_list_item_1
        result.adapter = object : ArrayAdapter<BluetoothDeviceWrapper?>(
            this, android.R.layout.simple_list_item_2,
            android.R.id.text1, list
        ) {
            override fun getView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getView(position, convertView, parent)
                val device: BluetoothDevice = devices[position].bluetoothDevice
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
