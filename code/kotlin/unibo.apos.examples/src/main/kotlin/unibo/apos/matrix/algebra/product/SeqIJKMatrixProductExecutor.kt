package unibo.apos.matrix.algebra.product

import unibo.apos.matrix.Matrix
import unibo.apos.matrix.createMatrix

class SeqIJKMatrixProductExecutor: MatrixProductExecutor {

    override fun multiply(matA: Matrix, matB: Matrix): Matrix {
        val res = createMatrix(matA.size, matB[0].size)
        for(i in matA.indices)
            for(j in matB[0].indices)
                for(k in matA[0].indices)
                    res[i][j] += matA[i][k] * matB[k][j]

        return res
    }


}