package unibo.apos.matrix

import unibo.apos.matrix.views.ColumnView
import unibo.apos.matrix.views.RowView
import java.lang.StringBuilder
import java.util.StringJoiner

/**
 * The matrix type alias for an [Array] of rows in which each element is the [DoubleArray]
 * which contains the row at the relative index.
 */
typealias Matrix = Array<DoubleArray>

/* CREATION ********************************************************************************************************* */
/**
 * Create a new empty [Matrix] of [Double].
 * Each element of the resulting matrix will be `0`.
 *
 * @param rows the number of the rows
 * @param columns the number of the columns
 * @return the new created matrix
 */
fun createMatrix(rows: Int, columns: Int) : Matrix {
    return Array(rows) { DoubleArray(columns) }
}

/**
 * Create a [Matrix] populating it with the values that comes from the [generator] function.
 * This function takes the number of the row and the number of the column and produces the element for
 * that position in the matrix
 *
 * @param rows the number of the rows
 * @param columns the number of the columns
 * @param generator the generator function that takes the number of the row and the number of the column
 * and produces the element to be placed in that position
 * @return the new populated matrix
 */
fun createMatrix(rows: Int, columns: Int, generator: (Int, Int) -> Double): Matrix {
    return Array(rows) { currentRowIdx ->
        DoubleArray(columns) { currentColumnIdx -> generator(currentRowIdx, currentColumnIdx)  }
        }
}

@DslMarker
internal annotation class MatrixDSL

/**
 * Defines a new row
 *
 * @constructor Create empty Row
 */
@MatrixDSL object row

/**
 * A builder for a [Matrix] which exposes the function [MatrixBuilder.row] to accumulate
 * the rows into an internal structure.
 * After all the rows have been accumulated, it is possible to invoke [MatrixBuilder.build] to
 * create and get the matrix
 *
 * @constructor Create empty Matrix builder
 */
@MatrixDSL
class MatrixBuilder {
    private val rows: MutableList<DoubleArray> = mutableListOf()

    /**
     * Adds a new row to the creating matrix by using the elements passed as parameters
     *
     * @param elements the elements to add to this row
     * @throws IllegalArgumentException if one row has a different size from the previous ones
     */
    @MatrixDSL
    @Throws(IllegalArgumentException::class)
    fun row(vararg elements: Double) {
        if(rows.isNotEmpty())
            if(elements.size != rows[0].size)
                throw IllegalArgumentException("the row [$elements] is not of the required length ${rows[0].size}")
        rows.add(elements)
    }

    /**
     * The equivalent of the [MatrixBuilder.row] method that let to add a row by using the [row] object
     * as a DSL keyword
     * @param elements the elements to add to this row
     * @throws IllegalArgumentException if one row has a different size from the previous ones
     */
    @MatrixDSL
    @Throws(IllegalArgumentException::class)
    operator fun row.get(vararg elements: Double) {
        this@MatrixBuilder.row(*elements)
    }

    @MatrixDSL
    fun build(): Matrix {
        return rows.toTypedArray()
    }
}

/**
 * Create a new matrix using the given `init` function.
 * This function lets to create a matrix *row-by-row* by using the [MatrixBuilder.row] function
 * to define a row. An example to define a matrix is:
 * ```
 * createMatrix() {
 *  row(1, 2, 3)
 *  row(4, 5, 6)
 * }
 * ```
 * which define the matrix:
 * ```
 * |1   2   3|
 * |4   5   6|
 * ```
 * This function will throw [IllegalArgumentException] if one row has a different size
 * from the others
 *
 * @param init the function to be passed to the [MatrixBuilder] in order to create the matrix
 * @receiver the [MatrixBuilder] that will be used to create the matrix
 * @return the created matrix
 */
@Throws(IllegalArgumentException::class)
fun createMatrix(init: MatrixBuilder.() -> Unit): Matrix {
    return MatrixBuilder().apply(init).build()
}

/* CONVERSION/RESHAPE *********************************************************************************************** */
/**
 * Creates and returns a new matrix starting from this [Array] of [DoubleArray].
 * This function reshape this array in order to make all the of the rows to have
 * the same length, so the resulting [Matrix] will have:
 *
 * - the number of the **rows** that will be `this.size`;
 * - the number of the **columns** that will be `this[j].size` in which `j` is the index of the
 * column with the maximum number of elements.
 *
 * Each column with a number of elements less than the one which has maximum will be filled with `0`.
 * The original array **will be not touched**, so this function **returns a copy** of this array
 *
 * @return the new created matrix
 */
fun Array<DoubleArray>.toMatrix(): Matrix {
    if(this.isEmpty())
        return arrayOf()

    return this.reshapedMatrix(this.size, this.maxOf { it.size })
}

/**
 * Reshape this [Array] of [DoubleArray] in order to be coherent with the definition of [Matrix] as
 * an array of rows with the same number of elements.
 * Then:
 * - if the number of the elements of one row is less than the [columns] argument, that row will be filled with
 * `0` to reach the desired size;
 * - if the number of the elements of one row is greater than the [columns] argument, that row will be truncated
 * to be adapted to the desired size.
 * By default, this function reshape this array in order to be rectangular, filling all the
 * rows with a number of the elements less than the greatest one
 *
 * The original array **will be not touched**, so this function **returns a copy** of this array.
 * Then, if this array already has the desired shape, this function will return **a copy** of this array.
 *
 * @param rows the number of rows the matrix will have
 * @param columns the number of the columns the matrix will have
 * @return the reshaped matrix
 */
fun Array<DoubleArray>.reshapedMatrix(rows: Int = this.size, columns: Int = this.maxOf { it.size }): Array<DoubleArray> {
    return Array(rows) { rowIdx ->
        return@Array if(rowIdx < this.size) {
            val currentRow = this[rowIdx]
            currentRow
                .copyInto(DoubleArray(columns), 0, 0, currentRow.size.coerceAtMost(columns))
        } else {
            DoubleArray(columns)
        }
    }
}

/* DIMENSIONS ******************************************************************************************************* */
/**
 * Count the number of the **rows** of this [Matrix]
 * @return the number of the rows in this matrix
 */
fun Matrix.countMatrixRows(): Int {
    return this.size
}

/**
 * Count the number of the **columns** of this [Matrix].
 * Since [Matrix] is a *typealias* for an [Array] of [DoubleArray], it is possible that
 * this array **could not semantically be a matrix**: in that case this function returns
 * the number of the elements of **the first row**.
 * Nevertheless, if this array has been created with a [createMatrix] builder, this method
 * returns exactly the number of the columns of this matrix
 *
 * @return the number of the columns in this matrix
 */
fun Matrix.countMatrixColumns(): Int {
    if(this.isEmpty())
        return 0
    return this[0].size
}

/**
 * Validates this [Array] of [DoubleArray] checking if each row has the same number of elements.
 * If not, this array is not coherent with the definition of [Matrix] and needs to be *converted* to
 * a matrix using the [toMatrix] or the [reshapedMatrix] function, so an [IllegalArgumentException] will be thrown
 */
@Throws(IllegalArgumentException::class)
fun Array<DoubleArray>.validateMatrixShapeOrThrows() {
    if(!hasMatrixShape())
        throw IllegalArgumentException()
}

/**
 * Checks if this [Array] of [DoubleArray] has the shape of a matrix, checking if each row has the same
 * number of elements.
 *
 * @return `true` if this array has each row with the same number of elements that means that has the shape of
 * a matrix, `false` otherwise
 */
fun Array<DoubleArray>.hasMatrixShape(): Boolean {
    if(this.isNotEmpty() && this.map { it.size }.distinct().count() != 1)
        return false
    return true
}

/* STRING CONVERSION ************************************************************************************************ */
/**
 * Creates and returns a [String] representation of this matrix
 *
 * @return a [String] representation of this matrix
 */
fun Matrix.matrixToString(): String {
    val sj = StringJoiner(",\n ", "[", "]")
    this.forEach { row ->
        val rowSj = StringJoiner(", ", "[", "]")
        row.forEach { rowSj.add(it.toString()) }
        sj.add(rowSj.toString())
    }
    return sj.toString()
}

/* VIEWS ************************************************************************************************************ */

/**
 * Create and returns a [ColumnView] over the desired column of the given [matrix]
 *
 * @param matrix the matrix
 * @param column the index of the column to be used
 * @return the [ColumnView] instance referring the desired [column] of the [matrix]
 */
fun getMatrixColumn(matrix: Array<DoubleArray>, column: Int): ColumnView {
    return ColumnView(matrix, column)
}

/**
 * Create and returns a [RowView] over the desired column of the given [matrix]
 *
 * @param matrix the matrix
 * @param row the index of the column to be used
 * @return the [RowView] instance referring the desired [row] of the [matrix]
 */
fun getMatrixRow(matrix: Array<DoubleArray>, row: Int): RowView {
    return RowView(matrix, row)
}