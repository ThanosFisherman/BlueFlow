/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.thanosfisherman.blueflow.sample

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import io.github.thanosfisherman.blueflow.BlueFlow
import io.github.thanosfisherman.blueflow.sample.usecase.BtConnectUseCase
import io.github.thanosfisherman.blueflow.sample.usecase.BtNavigationUseCase
import io.github.thanosfisherman.blueflow.sample.usecase.DiscoverBtDevicesUseCase
import io.github.thanosfisherman.blueflow.sample.viewmodel.ViewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

/**
 * Class that handles object creation.
 * Like this, objects can be passed as parameters in the constructors and then replaced for
 * testing, where needed.
 */
@FlowPreview
@ExperimentalCoroutinesApi
object Injection {

    private var context: Context? = null

    fun provideContext(context: Context) {
        this.context = context
    }

    private fun provideBtNavigationUseCases() = BtNavigationUseCase(
        BlueFlow.getInstance(
            context ?: throw NullPointerException("Context not provided")
        )
    )

    private fun provideBtDiscoveryUseCase() = DiscoverBtDevicesUseCase(
        BlueFlow.getInstance(
            context ?: throw NullPointerException("Context not provided")
        )
    )

    private fun provideBtConnectUseCase() = BtConnectUseCase(
        BlueFlow.getInstance(
            context ?: throw NullPointerException("Context not provided")
        )
    )

    /**
     * Provides the [ViewModelProvider.Factory] that is then used to get a reference to
     * [ViewModel] objects.
     */
    fun provideViewModelFactory(): ViewModelProvider.Factory {
        return ViewModelFactory(
            provideBtNavigationUseCases(), provideBtDiscoveryUseCase(), provideBtConnectUseCase()
        )
    }
}
