package unibo.apos.matrix.product.impl

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import unibo.apos.matrix.model.MatrixCell
import unibo.apos.matrix.model.MatrixPointer
import unibo.apos.matrix.product.MatrixProduct
import unibo.apos.matrix.utils.MatrixUtils

class FanChanneledMatrixProductImpl: MatrixProduct {

    override suspend fun multiply(matA: Array<IntArray>, matB: Array<IntArray>, workers: Int): Array<IntArray> {
        val rows = matA.size
        val cols = matB[0].size
        val cells = rows * cols
        val result = Array(rows) { IntArray(cols) }

        val taskChannel = Channel<MatrixPointer>(cells);
        val resultChannel = Channel<MatrixCell>(cells);

        coroutineScope {
            repeat(workers) { _ ->
                launch {
                    worker(taskChannel, resultChannel, matA, matB);
                }
            }

            distributeWork(taskChannel, rows, cols);
            collectResults(resultChannel, result, cells);
        }

        return result
    }

    private suspend fun collectResults(
        resultChannel: Channel<MatrixCell>,
        result: Array<IntArray>,
        totalCells: Int
    ) {
        repeat(totalCells) {
            val (pointer, value) = resultChannel.receive();
            result[pointer.row][pointer.col] = value;
        }
        resultChannel.close();
    }

    private suspend fun distributeWork(
        taskChannel: Channel<MatrixPointer>,
        rows: Int,
        cols: Int
    ) {
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                taskChannel.send(MatrixPointer(row, col));
            }
        }
        taskChannel.close();
    }

    private suspend fun worker(
        taskChannel: ReceiveChannel<MatrixPointer>,
        resultChannel: Channel<MatrixCell>,
        matA: Array<IntArray>,
        matB: Array<IntArray>
    ) {
        for (task in taskChannel) {
            resultChannel.send(MatrixUtils.computeProductCell(matA, matB, task))
        }
    }
}