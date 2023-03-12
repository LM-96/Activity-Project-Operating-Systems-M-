package unibo.apos.matrix.validation.exceptions

/**
 * An exception that could be thrown in the cases in which a matrix is invalid
 *
 * @property message the message of the exception
 * @property cause the cause of the exception
 * @constructor Create empty Matrix validation exception
 */
open class MatrixValidationException: Exception {
    constructor(message: String) : super(message)
    constructor(cause: Throwable) : super(cause)
}