package unibo.apos.matrix

import unibo.apos.matrix.types.IntMatrix
import unibo.apos.matrix.types.createIntMatrix

/**
 * The sequential implementation of the [IntMatrixProductExecutor] that uses
 * the classical `i`, `k`, `j` algorithm
 */
class SeqIKJIntMatrixProductExecutor {
    fun execute(matA: IntMatrix, matB: IntMatrix): IntMatrix {
        val rowsA = matA.size
        val colsA = matA[0].size
        val colsB = matB[0].size
        val matC = createIntMatrix(rowsA, colsB)

        for (i in 0 until rowsA) {
            for (k in 0 until colsA) {
                for (j in 0 until colsB) {
                    matC[i][j] += matA[i][k] * matB[k][j]
                }
            }
        }

        return matC
    }
}