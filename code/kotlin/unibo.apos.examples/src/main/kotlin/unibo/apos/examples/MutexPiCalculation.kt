package unibo.apos.examples

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.system.measureNanoTime


class SharedAddableDouble(
    private var value : Double = 0.0
) {

    private val mutex = Mutex()

    suspend fun add(quantity : Double) {
        mutex.withLock {
            value += quantity
        }
    }

    fun get() : Double {
        return value
    }
}

fun CoroutineScope.launchMutexPiWorker(sharedPi : SharedAddableDouble, k : Double) : Job {
    return launch {
        sharedPi.add(term(k))
    }
}

fun mutexPiCalculation(workers : Int) : PiCalculation {
    val sharedPi = SharedAddableDouble()
    val jobs = mutableListOf<Job>()
    val elapsedTime = measureNanoTime {
        runBlocking {
            for(k in 0 until workers) {
                jobs[k] = launchMutexPiWorker(sharedPi, k.toDouble())
            }
            jobs.joinAll()
        }
    }

    return PiCalculation(sharedPi.get(), workers, elapsedTime)

    //println("pi = $pi [ workers = $worker, elapsed time = ${elapsedTime.toDouble() / 1000000.0} millis ]")
}