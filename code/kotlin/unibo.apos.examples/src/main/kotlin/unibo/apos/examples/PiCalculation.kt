package unibo.apos.examples

import unibo.apos.utils.MenuEntry
import unibo.apos.utils.enabled
import unibo.apos.utils.readValidInt
import unibo.apos.utils.stdAutoMenuUntilExit
import kotlin.math.pow

data class PiCalculation(
    val pi: Double,
    val workers: Int,
    val nanoTime: Long
) {

    fun toStringWithMillis(): String {
        return "pi = $pi [ workers = $workers, elapsed time = ${nanoTime.toDouble() / 1000000.0} millis ]"
    }

}

enum class CalculationType {
    SHARED_MEMORY, MESSAGES_EXCHANGE
}

fun getPiCalculationFunction(type: CalculationType): (Int) -> PiCalculation {
    return when (type) {
        CalculationType.SHARED_MEMORY -> {
            { workers: Int -> mutexPiCalculation(workers) }
        }

        CalculationType.MESSAGES_EXCHANGE -> {
            { workers: Int -> channelPiCalculation(workers) }
        }
    }
}

fun piCalculationAvg(vararg calculations: PiCalculation): PiCalculation {
    val avgPi = calculations.map { it.pi }.average()
    val avgWorker = calculations.map { it.workers }.average().toInt()
    val avgNanos = calculations.map { it.nanoTime }.average().toLong()
    return PiCalculation(avgPi, avgWorker, avgNanos)
}

fun repeatedCalculation(
    type: CalculationType, repetitions: Int,
    workers: Int, onCalculation: (Int, PiCalculation) -> Unit = { _, _ -> }
): Array<PiCalculation> {
    val calculationFun = getPiCalculationFunction(type)
    val calculations = mutableListOf<PiCalculation>()
    var currentCalc: PiCalculation
    for (i in 0 until repetitions) {
        currentCalc = calculationFun(workers)
        calculations.add(currentCalc)
        onCalculation(i, currentCalc)
    }

    return calculations.toTypedArray()
}

fun term(k: Double): Double {
    return 4 * ((-1.0).pow(k) / (2.0 * k + 1))
}

fun main() {

    var workers = 5000
    var repetition = 5
    println("workers : $workers, repetitions : $repetition")

    val printCalculation = { num: Int, calc: PiCalculation ->
        println("\t$num : ${calc.toStringWithMillis()}")
    }
    val printAvg = { calcs: Array<PiCalculation> ->
        println("\n\tAVG : ${piCalculationAvg(*calcs).toStringWithMillis()}")
    }
    val onSharedMemorySelection = { _: MenuEntry ->
        println("\n** Calculating pi using shared memory ****** ")
        val calculations = repeatedCalculation(
            CalculationType.SHARED_MEMORY,
            repetition, workers, printCalculation
        )
        printAvg(calculations)
    }

    val onMessagesExchangeSelection = { _: MenuEntry ->
        println("\n** Calculating pi using messages exchange ****** ")
        val calculations = repeatedCalculation(
            CalculationType.MESSAGES_EXCHANGE,
            repetition, workers, printCalculation
        )
        printAvg(calculations)
    }
    stdAutoMenuUntilExit("PiCalculation Menu") {
        enabled entry "shared memory calculation" onSelection onSharedMemorySelection
        enabled entry "messages exchange calculation" onSelection onMessagesExchangeSelection
        enabled entry "both way calculation" onSelection {
            onSharedMemorySelection(it)
            onMessagesExchangeSelection(it)
        }
        enabled entry "set workers" onSelection {
            println("current workers: $workers")
            workers = readValidInt("insert the number of the workers")
        }
        enabled entry "set repetitions" onSelection {
            println("current repetitions: $repetition")
            repetition = readValidInt("insert the number of the repetitions")
        }
        onUserSelection {
            println("workers : $workers, repetitions : $repetition")
        }
    }

    println("bye :)")

}