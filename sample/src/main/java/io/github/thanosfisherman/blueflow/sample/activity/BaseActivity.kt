package io.github.thanosfisherman.blueflow.sample.activity

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import io.github.thanosfisherman.blueflow.sample.BluetoothActionEnum
import io.github.thanosfisherman.blueflow.sample.BtNativeDialogCallback
import io.github.thanosfisherman.blueflow.sample.BtNavigateState
import io.github.thanosfisherman.blueflow.sample.Injection
import io.github.thanosfisherman.blueflow.sample.viewmodel.BluetoothViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

const val REQUEST_ENABLE_BT_CODE = 222

@ExperimentalCoroutinesApi
@FlowPreview
abstract class BaseActivity : AppCompatActivity() {
    private val TAG = this.javaClass.simpleName

    protected lateinit var viewModel: BluetoothViewModel

    @get:LayoutRes
    protected abstract val layoutResId: Int
    private var callback: BtNativeDialogCallback? = null

    abstract fun executeBtCommand(params: BluetoothActionEnum)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutResId)
        viewModel =
            ViewModelProvider(this, Injection.provideViewModelFactory()).get(
                BluetoothViewModel::class.java
            )

        viewModel.btNavigationLive.observe(this) { observeBtNavigationState(it) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT_CODE) {
            if (resultCode == RESULT_OK)
                callback?.yes()
            else
                callback?.no()
        }
    }

    private fun notifyFragment(navigateState: BtNavigateState) {
        when (navigateState) {

            is BtNavigateState.BtShowNativeDialogNavigateState -> {
                callback = navigateState.callback
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT_CODE)
            }
        }
    }

    private fun observeBtNavigationState(btNavigateState: BtNavigateState) {

        when (btNavigateState) {
            is BtNavigateState.BtNotAvailableNavigateState -> {
                Toast.makeText(
                    applicationContext,
                    "THIS DEVICE DOES NOT SUPPORT BLUETOOTH",
                    Toast.LENGTH_LONG
                ).show()
            }
            is BtNavigateState.BtShowNativeDialogNavigateState -> {
                notifyFragment(btNavigateState)
            }
            is BtNavigateState.BtEnableSuccessNavigateState -> {
                executeBtCommand(btNavigateState.bluetoothActionEnum)
            }
        }
    }
}