package io.github.thanosfisherman.blueflow.sample

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex

class BlueFlowSampleApp : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}