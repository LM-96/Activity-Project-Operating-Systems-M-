package unibo.apos.matrix.product.impl

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import unibo.apos.matrix.model.Cell
import unibo.apos.matrix.model.CellPointer
import unibo.apos.matrix.product.MatrixProduct
import unibo.apos.matrix.utils.MatrixUtils

@Suppress("DUPLICATES")
class CoordinatorChanneledMatrixProductImpl: MatrixProduct {

    override suspend fun multiply(matA: Array<IntArray>, matB: Array<IntArray>, workers: Int): Array<IntArray> {
        val rows = matA.size
        val cols = matB[0].size
        val cells = rows * cols
        val result = Array(rows) { IntArray(cols) }

        val requestWorkChannel = Channel<Int>(Channel.BUFFERED);
        val ackChannel = Channel<Int>(Channel.BUFFERED);
        val resultChannel = Channel<Cell>(Channel.BUFFERED);
        val workerChannels = Array(workers) { Channel<CellPointer>(Channel.BUFFERED) }

        coroutineScope {
            repeat(workers) { id ->
                launch {
                    worker(id, requestWorkChannel, ackChannel, workerChannels[id], resultChannel, matA, matB)
                }
            }

            var currentRow = 0;
            var currentCol = 0;
            var completedCells = 0;
            try {
                while (completedCells < cells) {
                    select<Unit> {
                        if (currentRow < rows && currentCol < cols) {
                            requestWorkChannel.onReceive {
                                workerChannels[it].send(CellPointer(currentRow, currentCol))
                                currentCol++
                                if (currentCol == cols) {
                                    currentCol = 0;
                                    currentRow++
                                }
                            }
                        }

                        resultChannel.onReceive {
                            val cell = it.cell;
                            result[cell.row][cell.col] = it.value
                            completedCells++
                        }
                    }
                }
            } finally {
                workerChannels.forEach { it.send(CellPointer.EMPTY) }
                repeat(workers) { ackChannel.receive() }
                ackChannel.close()
                resultChannel.close()
            }
        }

        return result
    }

    private suspend fun worker(
        id: Int,
        requestWorkChannel: SendChannel<Int>,
        ackChannel: SendChannel<Int>,
        workerChannel: ReceiveChannel<CellPointer>,
        resultChannel: SendChannel<Cell>,
        matA: Array<IntArray>,
        matB: Array<IntArray>
    ) {
        requestWorkChannel.send(id);
        try {
            var pointer = workerChannel.receive()
            while (pointer != CellPointer.EMPTY) {
                val result = MatrixUtils.computeProductCell(matA, matB, pointer);
                resultChannel.send(result)
                requestWorkChannel.send(id);
                pointer = workerChannel.receive()
            }
        } finally {
            ackChannel.send(id);
        }
    }
}