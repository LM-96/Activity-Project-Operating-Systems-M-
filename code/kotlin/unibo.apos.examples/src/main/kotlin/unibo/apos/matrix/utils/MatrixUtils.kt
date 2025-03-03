package unibo.apos.matrix.utils

import unibo.apos.matrix.model.MatrixCell
import unibo.apos.matrix.model.MatrixPointer
import kotlin.random.Random

object MatrixUtils {

    fun computeProductCell(a: Array<IntArray>, b: Array<IntArray>, pointer: MatrixPointer): MatrixCell {
        var sum = 0
        for (k in a[0].indices) {
            sum += a[pointer.row][k] * b[k][pointer.col];
        }
        return MatrixCell(pointer, sum);
    }

    fun createRandomMatrix(size: Int): Array<IntArray> {
        return Array(size) {
            IntArray(size) {
                Random.nextInt(1, 10)
            }
        }
    }

}