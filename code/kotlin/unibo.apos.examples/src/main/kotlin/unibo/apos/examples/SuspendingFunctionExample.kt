package unibo.apos.examples

import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext

/**
 * Makes the coroutine that call this method sleeping for the specified time.
 * This method print a message to the console before the coroutine goes to sleep
 * and then another message after the resuming
 *
 * @param who the name of the coroutine that invokes the method
 * @param timeMillis the time the coroutine have to sleep in milliseconds
 */
suspend fun sleep(who: String, timeMillis: Long) {
    println("$who: I'm going to sleep for $timeMillis milliseconds...")
    delay(timeMillis)
    println("$who: Good morning, I wake up!")
}

/**
 * A method that executes an infinitive loop in which the coroutine that has called
 * it continuously *suspend* itself for the [pollingTime] and then print a message to the
 * console
 *
 * @param who the name of the coroutine that invokes the method
 * @param pollingTime the time of the polling (*sleep* time)
 */
suspend fun pollAlive(who: String, pollingTime: Long) {
    while (true) {
        delay(pollingTime)
        println("$who: i'm alive [thread=${Thread.currentThread()}]")
    }
}

/**
 * A simple method that prints the name of the coroutine that calls it.
 * The method also print the name of the thread that is currently executing the coroutine
 *
 * @param who the name of the coroutine that invokes the method
 */
suspend fun sayHello(who: String) {
    println(
        "$who : Hello... I'm a coroutine " +
                "[thread=${Thread.currentThread()}]"
    )
    println("$who : My context: $coroutineContext")
}

@OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
fun main() {
    val ctx = newSingleThreadContext("CoroutineSingleThread")

    runBlocking(ctx) {
        println("parent: [thread=${Thread.currentThread()}]")
        val job1 = launch {
            val who = "job1"
            sayHello(who)
            sleep(who, 3000)
            sayHello(who)
        }
        val job2 = launch {
            val who = "job2"
            sayHello(who)
            pollAlive("job2", 500)
            sayHello(who)
        }
        job1.join()
        println("parent: job1 = $job1, job2 = $job2")
        job2.cancelAndJoin()
        println("parent: job1 = $job1, job2 = $job2")
    }
}