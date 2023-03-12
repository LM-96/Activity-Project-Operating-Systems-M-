package unibo.apos.matrix.math.product

import unibo.apos.matrix.types.IntMatrix
import unibo.apos.matrix.types.createIntMatrix

/**
 * The sequential implementation of the [IntMatrixProductExecutor] that uses
 * the classical `i`, `j`, `k` algorithm
 *
 * @constructor Create empty Seq i j k int matrix product executor
 */
class SeqIJKIntMatrixProductExecutor: IntMatrixProductExecutor {

    override fun multiply(matA: IntMatrix, matB: IntMatrix): IntMatrix {
        val rowsA = matA.size
        val colsA = matA[0].size
        val colsB = matB[0].size
        val matC = createIntMatrix(rowsA, colsB)

        for (i in 0 until rowsA) {
            for (j in 0 until colsB) {
                for (k in 0 until colsA) {
                    matC[i][j] += matA[i][k] * matB[k][j]
                }
            }
        }

        return matC
    }

}