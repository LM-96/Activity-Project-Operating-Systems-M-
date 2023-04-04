package unibo.apos.matrix.algebra.product

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import unibo.apos.matrix.Matrix
import unibo.apos.matrix.createMatrix

class ParallelSingleChannelGuidedMPE(
    var concurrentUnits: Int = DEFAULT_CONCURRENT_UNITS
): MatrixProductExecutor{

    val scope: CoroutineScope = openChildrenScopeForProductExecutor(this::class.java)
    private lateinit var matA: Matrix
    private lateinit var matB: Matrix
    private lateinit var matC: Matrix

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun worker(myId: Int, receiveChannel: ReceiveChannel<Int>, ackChannel: SendChannel<Int>) {
        while (!receiveChannel.isClosedForReceive) {
            try {
                val i = receiveChannel.receive()
                for(k in matA[0].indices) {
                    for(j in matB[0].indices) {
                        matC[i][j] += matA[i][k] * matB[k][j]
                    }
                }
            } catch (_: ClosedReceiveChannelException) {
                ackChannel.send(myId)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun multiply(matA: Matrix, matB: Matrix): Matrix {
        this.matA = matA
        this.matB = matB
        this.matC = createMatrix(matA.size, matB[0].size)

        runBlocking {
            val ackChannel = Channel<Int>()
            val mainChannel = scope.produce {
                for(i in matA.indices) send(i)
            }

            for(i in 0 until concurrentUnits) {
                scope.launch { worker(i, mainChannel, ackChannel) }
            }

            for(i in 0 until  concurrentUnits) {
                ackChannel.receive()
            }
        }
        return matC
    }

}