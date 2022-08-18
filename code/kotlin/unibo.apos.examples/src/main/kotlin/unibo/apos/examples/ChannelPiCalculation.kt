package unibo.apos.examples

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.system.measureNanoTime

fun CoroutineScope.launchChannelPiWorker(channel : Channel<Double>, k : Double) {
    launch {
        channel.send(term(k))
    }
}

fun channelPiCalculation(workers : Int) : PiCalculation {

    var pi = 0.0
    val elapsedTime = measureNanoTime {
        val ch = Channel<Double>(Channel.UNLIMITED)

        runBlocking {
            for(k in 0..workers) {
                launchChannelPiWorker(ch, k.toDouble())
            }
            for(k in 0..workers) {
                pi += ch.receive()
            }
        }
    }

    return PiCalculation(pi, workers, elapsedTime)
    //println("pi = $pi [ workers = $worker, elapsed time = ${elapsedTime.toDouble() / 1000000.0} millis ]")

}

