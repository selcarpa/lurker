package utils

import kotlinx.coroutines.*
import kotlin.time.Duration


fun CoroutineScope.Ticker(
    duration: Duration,
    onTick: () -> Unit
) {
    this.launch(Dispatchers.Main) {
        while (true) {
            withContext(Dispatchers.IO) { onTick() }
            delay(duration)
        }
    }
}
