package unibo.apos.matrix.utils

import unibo.apos.matrix.model.Cell
import unibo.apos.matrix.model.CellPointer
import unibo.apos.matrix.model.ProductUnit
import kotlin.random.Random

object MatrixUtils {

    fun computeProductCell(a: Array<IntArray>, b: Array<IntArray>, pointer: CellPointer): Cell {
        var sum = 0
        for (k in a[0].indices) {
            sum += a[pointer.row][k] * b[k][pointer.col];
        }
        return Cell(pointer, sum);
    }

    fun computeProductCell(productUnit: ProductUnit): Cell {
        var sum = 0
        val row = productUnit.row
        val col = productUnit.col
        for (k in productUnit.row.indices) {
            sum += row[k] * col[k];
        }
        return Cell(productUnit.pointer, sum)
    }

    fun createRandomMatrix(size: Int): Array<IntArray> {
        return Array(size) {
            IntArray(size) {
                Random.nextInt(1, 10)
            }
        }
    }

}