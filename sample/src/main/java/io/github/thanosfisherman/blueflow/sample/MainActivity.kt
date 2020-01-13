package io.github.thanosfisherman.blueflow.sample

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.coroutineContext

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        runBlocking {
            createFlow()
            createFlow().buffer().safeCollect() {
                Log.i("Main", it.toString())
                if (it == 1) {
                    cancel()
                }
            }

        }
    }

    private suspend fun createFlow(): Flow<Int> {

        return flow {
            Log.i("MAIN", "createFlow Called")
            emit(1)
            emit(2)
        }
    }

}
/**
 * Only proceed with the given action if the coroutine has not been cancelled.
 * Necessary because Flow.collect receives items even after coroutine was cancelled
 * https://github.com/Kotlin/kotlinx.coroutines/issues/1265
 */
suspend inline fun <T> Flow<T>.safeCollect(crossinline action: suspend (T) -> Unit) {
    collect {
        coroutineContext.ensureActive()
        action(it)
    }
}