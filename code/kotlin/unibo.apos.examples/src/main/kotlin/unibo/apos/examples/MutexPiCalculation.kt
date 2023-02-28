package unibo.apos.examples

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.system.measureNanoTime


/**
 * @author Luca Marchegiani
 * A simple implementation of an AtomicDouble that can be safely shared by
 * multiple coroutines and that can be updated by adding a certain quantity
 * to its value. The implementation uses one mutex to lock the
 * object and manage the concurrency.
 */
class NaiveAtomicAddableDouble(
    private var value : Double = 0.0
) {

    private val mutex = Mutex()

    /**
     * A safe method to add a certain quantity to this `Double`.
     * The method can be called concurrently from multiple coroutines
     *
     * @param quantity the quantity to be added
     */
    suspend fun add(quantity : Double) {
        mutex.withLock {
            value += quantity
        }
    }

    /**
     * Returns the value of this double.
     * This method does not use the internal `Mutex`.
     *
     * @return the current value of this double
     */
    fun get() : Double {
        return value
    }
}

/**
 * Launch mutex pi worker
 *
 * @param sharedPi
 * @param k
 * @return
 */
fun CoroutineScope.launchMutexPiWorker(sharedPi : NaiveAtomicAddableDouble, k : Double) : Job {
    return launch {
        sharedPi.add(term(k))
    }
}

fun mutexPiCalculation(workers : Int) : PiCalculation {
    val sharedPi = NaiveAtomicAddableDouble()
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

fun main() {

}