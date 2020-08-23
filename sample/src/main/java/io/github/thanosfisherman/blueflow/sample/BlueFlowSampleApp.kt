package io.github.thanosfisherman.blueflow.sample

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class BlueFlowSampleApp : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
        Injection.provideContext(this)
    }
}