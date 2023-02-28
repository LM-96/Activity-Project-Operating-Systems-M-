package unibo.apos.matrix.types

/**
 * The alias for an *array of array* that is a matrix of integers
 */
typealias IntMatrix = Array<IntArray>

/**
 * Create a matrix of integers with the given number of rows and columns.
 * Each element of the matrix will be initialized to 0
 *
 * @param rows the number of the rows
 * @param columns the number of the columns
 * @return the new instance of the matrix
 */
fun createIntMatrix(rows: Int, columns: Int): IntMatrix {
    return Array(rows){ IntArray(columns) {0} }
}

/**
 * Create a matrix of integers with the given number of rows and columns.
 * Each element of the matrix will be initialized to the value returned by the `init` function that takes
 * the row and the column as parameters
 *
 * @param rows the number of the rows
 * @param columns the number of the columns
 * @param init the `init` function for each element of the matrix; the function will be used to
 * @return the new instance of the matrix
 */
fun createIntMatrix(rows: Int, columns: Int, init: (Int, Int) -> Int): IntMatrix {
    val res = arrayOfNulls<IntArray>(rows)
    for (row in 0..rows) {
        res[row] = IntArray(columns)
        for(col in 0..columns) {
            res[row]!![col] = init(row, col)
        }
    }

    @Suppress("UNCHECKED_CAST")
    return res as IntMatrix
}

class IntMatrixBuilder {

    private val rows: MutableList<IntArray> = mutableListOf()

    fun row(vararg elements: Int) {
        rows.add(elements)
    }

    fun build(): IntMatrix {
        return rows.toTypedArray()
    }
}

fun createIntMatrix(builder: IntMatrixBuilder.() -> Unit): IntMatrix {
    val builderInstance = IntMatrixBuilder()
    builderInstance.builder()
    return builderInstance.build()
}