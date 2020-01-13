package io.github.thanosfisherman.blueflow

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.text.TextUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.channelFlow

class BlueFlow(private val context: Context) {

    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    /**
     * Return true if Bluetooth is available.
     *
     * @return true if bluetoothAdapter is not null or it's address is empty, otherwise Bluetooth is
     * not supported on this hardware platform
     */
    fun isBluetoothAvailable() =
        !(bluetoothAdapter == null || TextUtils.isEmpty(bluetoothAdapter.address))

    /**
     * Return true if Bluetooth is currently enabled and ready for use.
     * <p>Equivalent to:
     * <code>getBluetoothState() == STATE_ON</code>
     * <p>Requires {@link android.Manifest.permission#BLUETOOTH}
     *
     * @return true if the local adapter is turned on
     */
    fun isBluetoothEnabled() = bluetoothAdapter.isEnabled

    /**
     * This will issue a request to enable Bluetooth through the system settings (without stopping
     * your application) via ACTION_REQUEST_ENABLE action Intent.
     *
     * @param activity Activity
     * @param requestCode request code
     */
    fun enableBluetooth(activity: Activity, requestCode: Int) {
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            activity.startActivityForResult(enableBtIntent, requestCode)
        }
    }

    /**
     * Turn on the local Bluetooth adapter — do not use without explicit user action to turn on
     * Bluetooth.
     *
     * @return true to indicate adapter startup has begun, or false on
     * immediate error
     * @see BluetoothAdapter.enable
     */
    fun enable() = bluetoothAdapter.enable()


    /**
     * Turn off the local Bluetooth adapter — do not use without explicit user action to turn off
     * Bluetooth.
     *
     * @return true to indicate adapter shutdown has begun, or false on
     * immediate error
     * @see BluetoothAdapter.enable
     */
    fun disable() = bluetoothAdapter.disable()

    /**
     * Return the set of [BluetoothDevice] objects that are bonded
     * (paired) to the local adapter.
     *
     * If Bluetooth state is not [BluetoothAdapter.STATE_ON], this API
     * will return an empty set. After turning on Bluetooth,
     * wait for [BluetoothAdapter.ACTION_STATE_CHANGED] with [BluetoothAdapter.STATE_ON]
     * to get the updated value.
     *
     * Requires [android.Manifest.permission.BLUETOOTH].
     *
     * @return unmodifiable set of [BluetoothDevice], or null on error
     */
    fun getBondedDevices(): Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices

    /**
     * Start the remote device discovery process.
     *
     * @return true on success, false on error
     */
    fun startDiscovery() = bluetoothAdapter.startDiscovery()

    /**
     * Return true if the local Bluetooth adapter is currently in the device
     * discovery process.
     *
     * @return true if discovering
     */
    fun isDiscovering() = bluetoothAdapter.isDiscovering

    /**
     * Cancel the current device discovery process.
     *
     * @return true on success, false on error
     */
    fun cancelDiscovery() = bluetoothAdapter.cancelDiscovery()

    /**
     * This will issue a request to make the local device discoverable to other devices. By default,
     * the device will become discoverable for 120 seconds.
     *
     * @param activity Activity
     * @param requestCode request code
     */
    fun enableDiscoverability(activity: Activity, requestCode: Int) {
        enableDiscoverability(activity, requestCode, -1)
    }

    /**
     * This will issue a request to make the local device discoverable to other devices. By default,
     * the device will become discoverable for 120 seconds.  Maximum duration is capped at 300
     * seconds.
     *
     * @param activity Activity
     * @param requestCode request code
     * @param duration discoverability duration in seconds
     */
    fun enableDiscoverability(activity: Activity, requestCode: Int, duration: Int) {
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        if (duration >= 0) {
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, duration)
        }
        activity.startActivityForResult(discoverableIntent, requestCode)
    }

    @ExperimentalCoroutinesApi
    fun discoverDevices() = channelFlow {
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (BluetoothDevice.ACTION_FOUND == intent?.action) {
                    val device =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice
                    offer(device)
                }
            }
        }
        context.registerReceiver(receiver, filter)

        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }

    @ExperimentalCoroutinesApi
    fun bluetoothState() = channelFlow {
        val filter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                offer(bluetoothAdapter.scanMode)
            }
        }
        context.registerReceiver(receiver, filter)
        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }
}