package unibo.apos.examples

import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext

suspend fun sleep(who : String, timeMillis : Long) {
    println("$who: I'm going to sleep for $timeMillis milliseconds...")
    delay(timeMillis)
    println("$who: Good morning, I wake up!")
}

suspend fun pollAlive(who : String, pollingTime : Long) {
    while (true) {
        delay(pollingTime)
        println("$who: i'm alive [thread=${Thread.currentThread()}]")
    }
}

suspend fun sayHello(who : String) {
    println("$who : Hello... I'm a coroutine " +
            "[thread=${Thread.currentThread()}]")
    println("$who : My context: $coroutineContext")
}

fun main() {
    @OptIn(DelicateCoroutinesApi::class)
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