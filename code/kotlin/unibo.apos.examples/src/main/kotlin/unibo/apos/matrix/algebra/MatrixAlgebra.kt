package unibo.apos.matrix.algebra

import unibo.apos.matrix.Matrix
import unibo.apos.matrix.algebra.product.MatrixMultiplier
import unibo.apos.matrix.algebra.product.SeqIJKMatrixMultiplier
import unibo.apos.matrix.exceptions.MatrixShapeException
import unibo.apos.matrix.validateMatrixShapeOrThrows

internal var MATRIX_MULTIPLIER: MatrixMultiplier = SeqIJKMatrixMultiplier()

/**
 * Performs the matrix multiplication between `this` (on the left side) and the given [matB] (on the right size) matrix,
 * returning the result. Then, this method performs `this * matB`.
 *
 * This method validates the two matrices throwing an exception is something goes wrong
 *
 * @param matB the right side matrix of the product
 * @return the result of the multiplication
 * @throws IllegalArgumentException if one of the two matrices is an [Array] od [DoubleArray] without the shape
 * of a matrix
 * @throws MatrixShapeException if the number of columns of `this` matrix is not the same as the number of the
 * rows of [matB]
 */
@Throws(IllegalArgumentException::class, MatrixShapeException::class)
fun Matrix.multiply(matB: Matrix): Matrix {
    this.validateMatrixShapeOrThrows()
    matB.validateMatrixShapeOrThrows()
    if (this[0].size != matB.size)
        throw MatrixShapeException("this matrix has ${this[0].size} columns but matB has ${matB.size} rows")

    return MATRIX_MULTIPLIER.multiply(this, matB)
}