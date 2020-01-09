package io.github.thanosfisherman.blueflow.sample

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        runBlocking {
            createFlow().collect {
                Log.i("Main", it.toString())
                if (it == 1) {
                    cancel()
                }
            }

        }
    }

    private suspend fun createFlow(): Flow<Int> {

        val pls = flow {
            emit(1)
            emit(2)
        }
        return pls
    }

}
