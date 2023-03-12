package unibo.apos.matrix.types

/**
 * The alias for an *array of array* that is a matrix
 */
typealias Matrix<T> = Array<Array<T>>

/**
 * Create a matrix of elements of type `T` using the given `init` function
 *
 * @param T the type of the elements inside the matrix
 * @param rows the number of the rows
 * @param columns the number of the columns
 * @param init the `init` function for each element of the matrix; the function will be used to
 * generate the value of each element of the matrix considering the row and the column
 *
 * @return the new instance of the matrix
 */
inline fun <reified T> createMatrix(rows: Int, columns: Int, init: (Int, Int) -> T): Matrix<T> {
    val res = arrayOfNulls<Array<T?>>(rows)
    for (row in 0..rows) {
        res[row] = arrayOfNulls<T>(columns)
        for(col in 0..columns) {
            res[row]!![col] = init(row, col)
        }
    }

    @Suppress("UNCHECKED_CAST")
    return res as Matrix<T>
}

/**
 * Create a matrix of elements of type `T` with null values
 *
 * @param T the type of the elements inside the matrix
 * @param rows the number of the rows
 * @param columns the number of the columns
 * @return the new instance of the matrix
 */
inline fun <reified T> createMatrix(rows: Int, columns: Int): Matrix<T?> {
    return Array(rows){ arrayOfNulls<T>(columns) }
}