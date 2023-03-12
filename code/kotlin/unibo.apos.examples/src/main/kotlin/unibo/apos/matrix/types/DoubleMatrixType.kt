package unibo.apos.matrix.types

import unibo.apos.matrix.validation.exceptions.InvalidRowsSizeException

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
    for (row in 0 until rows) {
        res[row] = DoubleArray(columns)
        for (col in 0 until columns) {
            res[row]!![col] = init(row, col)
        }
    }

    @Suppress("UNCHECKED_CAST")
    return res as DoubleMatrix
}

@DslMarker
annotation class DoubleMatrixDSL // This annotation is used to mark the DSL functions

@DoubleMatrixDSL
class DoubleMatrixBuilder {

    private val rows: MutableList<DoubleArray> = mutableListOf()

    /**
     * Defines a *row* of elements
     *
     * @param elements the element to be added to the row
     * @throws InvalidRowsSizeException if the row has an invalid size
     */
    @Throws(InvalidRowsSizeException::class)
    fun row(vararg elements: Double) {
        if(rows.isNotEmpty()) {
            if(rows[0].size != elements.size) {
                throw InvalidRowsSizeException(elements.toTypedArray(), rows[0].size)
            }
        }
        rows.add(elements)
    }

    fun build(): DoubleMatrix {
        return rows.toTypedArray()
    }
}

/**
 * Create a matrix of [DoubleMatrix] using the DSL
 *
 * @param init the `init` function that will be used to create the matrix
 * @return the new instance of the matrix
 */
fun createDoubleMatrix(init: DoubleMatrixBuilder.() -> Unit): DoubleMatrix {
    val builder = DoubleMatrixBuilder()
    builder.init()
    return builder.build()
}

/**
 * Return a string representation of the matrix of double in a tabular format
 *
 * @return the string representation of the matrix
 */
fun DoubleMatrix.toTabularString(): String {
    val stringBuilder = StringBuilder()
    for (row in this) {
        stringBuilder.append(row.joinToString("\t"))
        stringBuilder.append("\n")
    }
    return stringBuilder.toString()
}

/**
 * Prints this matrix of doubles to the standard output
 */
fun DoubleMatrix.print() {
    println(this.toTabularString())
}

/**
 * Returns the number of rows of this matrix
 *
 * @return the number of rows of this matrix
 */
fun DoubleMatrix.getNumberOfRows(): Int {
    return this.size
}

/**
 * Returns the number of columns of this matrix
 *
 * @return the number of columns of this matrix
 */
fun DoubleMatrix.getNumberOfColumns(): Int {
    return this[0].size
}