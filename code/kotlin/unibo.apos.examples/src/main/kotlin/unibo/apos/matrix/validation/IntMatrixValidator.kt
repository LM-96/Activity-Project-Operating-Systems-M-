package unibo.apos.matrix.validation

import unibo.apos.matrix.types.IntMatrix
import unibo.apos.matrix.validation.exceptions.MatrixValidationException

/**
 * A validator for an [IntMatrix]
 *
 * @constructor Create empty Int matrix validator
 */
fun interface IntMatrixValidator {

    /**
     * Validates the given matrix following the implementation logic.
     * @param matrix the matrix to validate
     * @param throwError if true, throws an exception if the matrix is not valid
     * @return true if the matrix is valid, false otherwise
     * @throws MatrixValidationException if the matrix is not valid and [throwError] is true
     */
    @Throws(MatrixValidationException::class)
    fun validate(matrix: IntMatrix, throwError: Boolean): Boolean

    /**
     * Returns a lexicographic-order validator with another validator
     *
     * @param other the other validator
     * @return a lexicographic-order validator with the other validator
     */
    fun thenValidating(other: IntMatrixValidator): IntMatrixValidator = IntMatrixValidator { matrix, throwError ->
        val isValid = validate(matrix, throwError)
        if (isValid) {
            other.validate(matrix, throwError)
        } else {
            false
        }
    }

}