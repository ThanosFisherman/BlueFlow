package io.github.thanosfisherman.blueflow.sample.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.github.thanosfisherman.blueflow.sample.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

private const val REQUEST_PERMISSION_COARSE_LOCATION = 0
private const val REQUEST_ENABLE_BT = 1

@FlowPreview
@ExperimentalCoroutinesApi
class MainActivity : BaseActivity() {


    override val layoutResId: Int = R.layout.activity_main
    override fun executeBtCommand() {
        Log.i("MAIN", "SHOULD START DISCOVERY HERE")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                viewModel.initBtNavigation()
            }
        }
    }
}
