package unibo.apos.matrix.validation

/**
 * An exception that could be thrown in the cases in which a matrix has an invalid shape
 *
 * @property rows the number of rows
 * @property columns the number of columns
 * @property expectedRows the expected number of rows
 * @property expectedColumns the expected number of columns
 * @constructor Create empty Matrix shape exception
 */
class MatrixShapeException(val rows: Int, val columns: Int, val expectedRows: Int, val expectedColumns: Int) :
    Exception("The matrix has an invalid shape of $rows x $columns instead of $expectedRows x $expectedColumns")