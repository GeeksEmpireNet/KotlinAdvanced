package net.geeksempire.advanced.kotlin

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.actor
import kotlin.system.*

class SyncCoroutines {

    init {
        sharedCoroutinesActor()
    }

    suspend fun massiveRun(action: suspend () -> Unit) {
        val n = 100  // number of coroutines to launch
        val k = 1000 // times an action is repeated by each coroutine
        val time = measureTimeMillis {
            coroutineScope { // scope for coroutines
                repeat(n) {
                    launch {
                        repeat(k) {
                            action()
                        }
                    }
                }
            }
        }
        println("Completed ${n * k} actions in $time ms")
    }

    val counterContext = newSingleThreadContext("CounterContext")
    var counter = 0

    fun sharedCoroutines() = runBlocking {
        // confine everything to a single-threaded context
        withContext(counterContext) {
            massiveRun {
                counter++
            }
        }
        println("Counter = $counter")
    }

    // This function launches a new counter actor
    fun CoroutineScope.counterActor() = actor<CounterMsg> {
        var counter = 0 // actor state

        for (msg in channel) { // iterate over incoming messages
            when (msg) {
                is IncCounter -> {
                    counter++
                }
                is GetCounter -> {
                    msg.response.complete(counter)
                }
            }
        }
    }

    fun sharedCoroutinesActor() = runBlocking<Unit> {
        val counter = counterActor() // create the actor
        withContext(Dispatchers.Default) {
            massiveRun {
                counter.send(IncCounter)
            }
        }
        // send a message to get a counter value from an actor
        val response = CompletableDeferred<Int>()
        counter.send(GetCounter(response))
        println("Counter = ${response.await()}")
        counter.close() // shutdown the actor
    }


}

// Message types for counterActor
sealed class CounterMsg
object IncCounter : CounterMsg() // one-way message to increment counter
class GetCounter(val response: CompletableDeferred<Int>) : CounterMsg() // a request with reply