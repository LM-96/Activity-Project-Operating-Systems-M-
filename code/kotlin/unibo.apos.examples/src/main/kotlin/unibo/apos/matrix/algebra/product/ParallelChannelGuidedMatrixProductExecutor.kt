package unibo.apos.matrix.algebra.product

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import unibo.apos.matrix.Matrix
import unibo.apos.matrix.createMatrix

class ParallelChannelGuidedIJKMatrixProductExecutor(
    var concurrentUnits: Int = 10
): MatrixProductExecutor{

    val scope: CoroutineScope = openChildrenScopeForProductExecutor(ParallelChannelGuidedIJKMatrixProductExecutor::class.java)
    private lateinit var matA: Matrix
    private lateinit var matB: Matrix
    private lateinit var matC: Matrix

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun worker(myId: Int, receiveChannel: ReceiveChannel<Int>, ackChannel: SendChannel<Int>) {
        while (!receiveChannel.isClosedForReceive) {
            try {
                ackChannel.send(myId)
                val i = receiveChannel.receive()
                for(k in matA[0].indices) {
                    for(j in matB[0].indices) {
                        matC[i][j] += matA[i][k] * matB[k][j]
                    }
                }
            } catch (crce: ClosedReceiveChannelException) {
                return
            }
        }
    }

    override fun multiply(matA: Matrix, matB: Matrix): Matrix {
        this.matA = matA
        this.matB = matB
        this.matC = createMatrix(matA.size, matB[0].size)

        val ackChannel = Channel<Int>()
        val workerChannels = IntRange(0, concurrentUnits - 1)
            .map { i -> Channel<Int>()
                .apply {
                    scope.launch {
                        worker(i, this@apply, ackChannel)
                    }
                }
            }

        runBlocking {
            var workerId: Int
            for(i in matA.indices) {
                workerId = ackChannel.receive()
                workerChannels[workerId].send(i)
            }

            for(i in 0 until  concurrentUnits) {
                workerId = ackChannel.receive()
                workerChannels[workerId].close()
            }
        }
        return matC
    }

}