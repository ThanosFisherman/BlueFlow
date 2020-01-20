package io.github.thanosfisherman.blueflow

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Parcelable
import android.text.TextUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import java.util.*

class BlueFlow(private val context: Context) {

    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    /**
     * Return true if Bluetooth is available.
     *
     * @return true if bluetoothAdapter is not null or it's address is empty, otherwise Bluetooth is
     * not supported on this hardware platform
     */
    @SuppressLint("HardwareIds")
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
     * Return true if a location service is enabled.
     *
     * @return true if either the GPS or Network provider is enabled
     */
    fun isLocationServiceEnabled(): Boolean {
        val locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    /**
     * Helper class for simplifying read and write operations from/to {@link BluetoothSocket}.
     *
     * @param socket bluetooth socket
     * @throws Exception if can't get input/output stream from the socket
     */
    fun setupIO(bluetoothSocket: BluetoothSocket) = BlueFlowIO(bluetoothSocket)

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
    fun bondedDevices(): Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices

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

    /**
     * Observes Bluetooth devices found while discovering.
     *
     * @return Flow Observable with BluetoothDevice found
     */
    @ExperimentalCoroutinesApi
    fun discoverDevices() = callbackFlow {
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
    }.flowOn(Dispatchers.IO)

    /**
     * Observes DiscoveryState, which can be ACTION_DISCOVERY_STARTED or ACTION_DISCOVERY_FINISHED
     * from {@link BluetoothAdapter}.
     *
     * @return Flow Observable with DiscoveryState
     */
    @ExperimentalCoroutinesApi
    fun discoveryState() = callbackFlow {
        val filter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let { offer(it.action) }
            }
        }
        context.registerReceiver(receiver, filter)
        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Observes BluetoothState. Possible values are:
     * {@link BluetoothAdapter#STATE_OFF},
     * {@link BluetoothAdapter#STATE_TURNING_ON},
     * {@link BluetoothAdapter#STATE_ON},
     * {@link BluetoothAdapter#STATE_TURNING_OFF},
     *
     * @return Flow Observable with BluetoothState
     */
    @ExperimentalCoroutinesApi
    fun bluetoothState() = callbackFlow {
        val filter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                offer(bluetoothAdapter.state)
            }
        }
        context.registerReceiver(receiver, filter)
        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Observes scan mode of device. Possible values are:
     * {@link BluetoothAdapter#SCAN_MODE_NONE},
     * {@link BluetoothAdapter#SCAN_MODE_CONNECTABLE},
     * {@link BluetoothAdapter#SCAN_MODE_CONNECTABLE_DISCOVERABLE}
     *
     * @return Flow Observable with scan mode
     */
    @ExperimentalCoroutinesApi
    fun scanMode() = callbackFlow {
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
    }.flowOn(Dispatchers.IO)

    /**
     * Observes connection to specified profile. See also {@link BluetoothProfile.ServiceListener}.
     *
     * @param bluetoothProfile bluetooth profile to connect to. Can be either {@link
     * BluetoothProfile#HEALTH},{@link BluetoothProfile#HEADSET}, {@link BluetoothProfile#A2DP},
     * {@link BluetoothProfile#GATT} or {@link BluetoothProfile#GATT_SERVER}.
     * @return Flow Observable with {@link ServiceEvent}
     */
    @ExperimentalCoroutinesApi
    fun bluetoothProfile(bluetoothProfile: Int): Flow<ServiceEvent> = callbackFlow {

        val listener = object : BluetoothProfile.ServiceListener {
            var proxy: BluetoothProfile? = null
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
                this.proxy = proxy
                offer(ServiceEvent(ServiceEvent.State.CONNECTED, profile, proxy))
            }

            override fun onServiceDisconnected(profile: Int) {
                offer(ServiceEvent(ServiceEvent.State.DISCONNECTED, profile, null))
            }
        }

        if (!bluetoothAdapter.getProfileProxy(context, listener, bluetoothProfile)) {
            throw ProfileProxyException()
        }
        awaitClose {
            listener.proxy?.let {
                bluetoothAdapter.closeProfileProxy(bluetoothProfile, it)
            }
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Close the connection of the profile proxy to the Service.
     *
     *
     *  Clients should call this when they are no longer using the proxy obtained from [ ][.observeBluetoothProfile].
     *
     * Profile can be one of [BluetoothProfile.HEALTH],[BluetoothProfile.HEADSET],
     * [BluetoothProfile.A2DP], [BluetoothProfile.GATT] or [ ][BluetoothProfile.GATT_SERVER].
     *
     * @param profile the Bluetooth profile
     * @param proxy profile proxy object
     */
    fun closeProfileProxy(profile: Int, proxy: BluetoothProfile) =
        bluetoothAdapter.closeProfileProxy(profile, proxy)


    /**
     * Observes connection state of devices.
     *
     * @return Flow Observable with {@link ConnectionStateEvent}
     */
    @ExperimentalCoroutinesApi
    fun connectionState() = callbackFlow {
        val filter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let {
                    val status = it.getIntExtra(
                        BluetoothAdapter.EXTRA_CONNECTION_STATE,
                        BluetoothAdapter.STATE_DISCONNECTED
                    )
                    val previousStatus = it.getIntExtra(
                        BluetoothAdapter.EXTRA_PREVIOUS_CONNECTION_STATE,
                        BluetoothAdapter.STATE_DISCONNECTED
                    )
                    val device =
                        it.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as? BluetoothDevice
                    offer(ConnectionStateEvent(status, previousStatus, device))
                }
            }
        }
        context.registerReceiver(receiver, filter)
        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Observes bond state of devices.
     *
     * @return Flow Observable with {@link BondStateEvent}
     */
    @ExperimentalCoroutinesApi
    fun bondState() = callbackFlow {
        val filter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let {
                    val state =
                        it.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE)
                    val previousState = it.getIntExtra(
                        BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE,
                        BluetoothDevice.BOND_NONE
                    )
                    val device =
                        it.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as? BluetoothDevice
                    offer(BondStateEvent(state, previousState, device))
                }
            }
        }
        context.registerReceiver(receiver, filter)
        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Opens {@link BluetoothServerSocket}, listens for a single connection request, releases socket
     * and returns a connected {@link BluetoothSocket} on successful connection. Notifies observers
     * with {@link IOException} {@code onError()}.
     *
     * @param name service name for SDP record
     * @param uuid uuid for SDP record
     * @param secure connection security status
     * @return Single with connected {@link BluetoothSocket} on successful connection
     */
    suspend fun connectAsServerAsync(
        name: String,
        uuid: UUID,
        secure: Boolean
    ): Deferred<BluetoothSocket> =
        coroutineScope {
            return@coroutineScope async(Dispatchers.IO) {
                val bluetoothServerSocket: BluetoothServerSocket = if (secure)
                    bluetoothAdapter.listenUsingRfcommWithServiceRecord(name, uuid)
                else
                    bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(name, uuid)

                bluetoothServerSocket.accept()
            }
        }

    /**
     * Create connection to {@link BluetoothDevice} and returns a connected {@link BluetoothSocket}
     * on successful connection. Notifies observers with {@link IOException} via {@code onError()}.
     *
     * @param bluetoothDevice bluetooth device to connect
     * @param uuid uuid for SDP record
     * @param secure connection security status
     * @return Deferred with connected {@link BluetoothSocket} on successful connection
     */
    suspend fun connectAsClientAsync(
        bluetoothDevice: BluetoothDevice,
        uuid: UUID,
        secure: Boolean
    ): Deferred<BluetoothSocket> =
        coroutineScope {
            return@coroutineScope async(Dispatchers.IO) {
                val bluetoothSocket =
                    if (secure) bluetoothDevice.createRfcommSocketToServiceRecord(uuid)
                    else bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid)
                bluetoothSocket.also { it.connect() }
            }
        }

    /**
     * Create connection to {@link BluetoothDevice} via createRfcommSocket and returns a connected {@link BluetoothSocket}
     * on successful connection.
     * Note: createRfcommSocket is not public API and hence this might break in the future.
     * Notifies observers with {@link IOException} or any reflection related exception via {@code onError()}.
     *
     * @param bluetoothDevice bluetooth device to connect
     * @param channel RFCOMM channel to connect to
     * @return Deferred with connected {@link BluetoothSocket} on successful connection
     * */
    suspend fun connectAsClientAsync(
        bluetoothDevice: BluetoothDevice, channel: Int
    ): Deferred<BluetoothSocket> = coroutineScope {
        return@coroutineScope async(Dispatchers.IO) {
            val bluetoothSocket = bluetoothDevice.createRfcommSocket(channel)
            bluetoothSocket.also { it.connect() }
        }
    }

    /**
     * Observes ACL broadcast actions from {@link BluetoothDevice}. Possible broadcast ACL action
     * values are:
     * {@link BluetoothDevice#ACTION_ACL_CONNECTED},
     * {@link BluetoothDevice#ACTION_ACL_DISCONNECT_REQUESTED},
     * {@link BluetoothDevice#ACTION_ACL_DISCONNECTED}
     *
     * @return Flow Observable with {@link AclEvent}
     */
    @ExperimentalCoroutinesApi
    fun AclEvents() = callbackFlow {
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let {
                    val device =
                        it.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as? BluetoothDevice
                    offer(AclEvent(it.action ?: "", device))
                }
            }
        }
        context.registerReceiver(receiver, filter)
        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Performs a service discovery and fetches a list of UUIDs that can be used to connect to {@link BluetoothDevice}
     *
     * @param bluetoothDevice bluetooth device to connect
     * @return Flow Observable with an array of Device UUIDs that can be used to connect to the device
     */
    @ExperimentalCoroutinesApi
    fun fetchDeviceUuids() = callbackFlow {
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_UUID)
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let { intnt ->
                    val uuidArray =
                        intnt.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID) as? Array<Parcelable>
                    uuidArray?.let { offer(it.toList()) }
                }
                try {
                    //Maybe I don't need to cancel prematurely
                    cancel()
                } catch (e: IllegalStateException) {
                }
            }
        }

        context.registerReceiver(receiver, filter)
        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }.flowOn(Dispatchers.IO)
}