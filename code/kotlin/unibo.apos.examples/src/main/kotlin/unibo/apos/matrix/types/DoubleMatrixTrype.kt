package unibo.apos.matrix.types

/**
 * The alias for an *array of array* that is a matrix of doubles
 */
typealias DoubleMatrix = Array<DoubleArray>

/**
 * Create a matrix of doubles with the given number of rows and columns.
 * Each element of the matrix will be initialized to 0.0
 *
 * @param rows the number of the rows
 * @param columns the number of the columns
 * @return the new instance of the matrix
 */
fun createDoubleMatrix(rows: Int, columns: Int): DoubleMatrix {
    return Array(rows){ DoubleArray(columns) {0.0} }
}

/**
 * Create a matrix of doubles with the given number of rows and columns.
 * Each element of the matrix will be initialized to the value returned by the `init` function that takes
 * the row and the column as parameters
 *
 * @param rows the number of the rows
 * @param columns the number of the columns
 * @param init the `init` function for each element of the matrix; the function will be used to
 * @return the new instance of the matrix
 */
fun createDoubleMatrix(rows: Int, columns: Int, init: (Int, Int) -> Double): DoubleMatrix {
    val res = arrayOfNulls<DoubleArray>(rows)
    for (row in 0..rows) {
        res[row] = DoubleArray(columns)
        for (col in 0..columns) {
            res[row]!![col] = init(row, col)
        }
    }

    @Suppress("UNCHECKED_CAST")
    return res as DoubleMatrix
}