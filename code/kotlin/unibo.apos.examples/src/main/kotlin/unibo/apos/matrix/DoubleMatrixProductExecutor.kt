package unibo.apos.matrix

import unibo.apos.matrix.types.Matrix

/**
 * A component that is able to calculate the **product** between two matrices
 * of `Double` values
 */
interface DoubleMatrixProductExecutor {

    /**
     * Executes the product between the given matrices respecting the
     * rules for the matrix product.
     * The operation is intended as `matC = matA * matB`
     *
     *
     * @param matA the left side matrix
     * @param matB the right side matrix
     * @return the result of the product (`matC`)
     */
    fun execute(matA: Matrix<Double>, matB: Matrix<Double>): Matrix<Double>
}