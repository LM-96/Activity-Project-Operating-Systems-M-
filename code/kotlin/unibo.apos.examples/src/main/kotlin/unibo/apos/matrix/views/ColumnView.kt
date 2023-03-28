package unibo.apos.matrix.views

import unibo.apos.matrix.countMatrixColumns

/**
 * A view over a column of a matrix.
 * Every change on this view will affect the matrix
 *
 *
 * @property matrix the matrix the view will refer to
 * @property column the column of the [matrix] this view will refer to
 * @constructor Create empty Column view
 * @throws IndexOutOfBoundsException if the [column] argument is not valid (less than `0` or greater than the last
 * index in the matrix)
 */
class ColumnView(
    private val matrix: Array<DoubleArray>,
    private val column: Int
) {

    init {
        if (column < 0 || column >= matrix.countMatrixColumns())
            throw IndexOutOfBoundsException("column index $column is not valid for matrix with columns ${matrix.countMatrixColumns()}")
    }

    operator fun get(row: Int): Double {
        return matrix[row][column]
    }

    operator fun set(row: Int, value: Double) {
        matrix[row][column] = value
    }

    /**
     * Returns the size of the column the view refers to
     *
     * @return the size of the column the view refers to
     */
    fun getSize(): Int {
        return matrix.size
    }

    /**
     * Returns an array that contains all the elements of the column this view refer to.
     * Every change in the returning array **will not affect** the column in the matrix
     *
     * @return an array that contains all the elements of the column this view refer to
     */
    fun toArray(): DoubleArray {
        return matrix.map { it[column] }
            .toDoubleArray()
    }

    override operator fun equals(other: Any?): Boolean {
        if (other == null)
            return false

        if (other is DoubleArray) {
            return !IntRange(0, getSize())
                .map { matrix[it][column] == other[it] }
                .contains(false)
        }

        if (other is IntArray) {
            return !IntRange(0, getSize())
                .map { matrix[it][column] == other[it].toDouble() }
                .contains(false)
        }

        if (other is ColumnView)
            return !IntRange(0, getSize())
                .map { matrix[it][column] == other.matrix[it][other.column] }
                .contains(false)

        return false
    }

    /**
     * Check if the row referred by this view is structurally equals to [other].
     * The [other] parameter could be a [DoubleArray], an [IntArray], a [RowView] or a [ColumnView]
     *
     * @param other
     * @return
     */
    fun contentEquals(other: Any?): Boolean {
        if (other == null)
            return false

        if (this == other)
            return true

        if (other is RowView) {
            return toArray().contentEquals(other.toArray())
        }

        return false
    }
}