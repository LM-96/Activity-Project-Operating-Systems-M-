package unibo.apos.matrix.algebra.product

import unibo.apos.matrix.Matrix

@FunctionalInterface
fun interface MatrixMultiplier {

    /**
     * Executes the matrix multiplication between the two matrices passed as arguments.
     * Then, this function performs `matA * matB` returning the result
     *
     * @param matA the first matrix
     * @param matB the second matrix
     * @return the multiplication between the two matrices
     */
    fun multiply(matA: Matrix, matB: Matrix): Matrix

}