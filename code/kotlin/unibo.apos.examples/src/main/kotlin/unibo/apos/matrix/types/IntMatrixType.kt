package unibo.apos.matrix.types

import unibo.apos.matrix.validation.exceptions.InvalidRowException
import unibo.apos.matrix.validation.exceptions.InvalidRowsSizeException
import java.util.*

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
    for (row in 0 until rows) {
        res[row] = IntArray(columns)
        for(col in 0 until columns) {
            res[row]!![col] = init(row, col)
        }
    }

    @Suppress("UNCHECKED_CAST")
    return res as IntMatrix
}

@DslMarker
annotation class IntMatrixDSL // This annotation is used to mark the DSL functions

@IntMatrixDSL
class IntMatrixBuilder {

    private val rows: MutableList<IntArray> = mutableListOf()

    /**
     * Defines a *row* of elements
     *
     * @param elements the element to be added to the row
     */
    @Throws(InvalidRowsSizeException::class)
    fun row(vararg elements: Int) {
        if(rows.isNotEmpty()) {
            if(rows[0].size != elements.size) {
                throw InvalidRowsSizeException(elements.toTypedArray(), rows[0].size)
            }
        }
        rows.add(elements)
    }

    fun build(): IntMatrix {
        return rows.toTypedArray()
    }
}

/**
 * Create a new instance of [IntMatrix] using the dedicated DSL
 *
 * @param init the function that will be used to build the matrix
 * @receiver the builder for the matrix that easily let to add rows
 * @return the new instance of the matrix
 * @throws InvalidRowException if the rows have different sizes
 */
@Throws(InvalidRowException::class)
fun createIntMatrix(init: IntMatrixBuilder.() -> Unit): IntMatrix {
    val builderInstance = IntMatrixBuilder()
    builderInstance.init()
    return builderInstance.build()
}

/**
 * Prints the given matrix of integers
 *
 * @param matrix the matrix to be printed
 */
fun intMatPrintln(matrix: IntMatrix) {
    for (row in matrix) {
        println(row.joinToString("\t"))
    }
}

/**
 * Returns a string representation of the given matrix of integers in a tabular format
 *
 * @return the string representation of the matrix
 */
fun IntMatrix.toTabularString(): String {
    val stringBuilder = StringBuilder()
    for (row in this) {
        stringBuilder.append(row.joinToString("\t"))
        stringBuilder.append("\n")
    }
    return stringBuilder.toString()
}

/**
 * Prints this matrix of integers to the standard output
 */
fun IntMatrix.print() {
    println(this.toTabularString())
}

/**
 * Returns the number of rows of this matrix
 *
 * @return the number of rows of this matrix
 */
fun IntMatrix.getNumberOfRows(): Int {
    return this.size
}

/**
 * Returns the number of columns of this matrix
 *
 * @return the number of columns of this matrix
 */
fun IntMatrix.getNumberOfColumns(): Int {
    return this[0].size
}