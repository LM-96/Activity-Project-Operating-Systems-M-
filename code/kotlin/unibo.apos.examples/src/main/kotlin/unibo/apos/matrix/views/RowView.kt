package unibo.apos.matrix.views

import unibo.apos.matrix.countMatrixRows

/**
 * A view over a row of a matrix.
 * Every change on this view will affect the matrix
 *
 *
 * @property matrix the matrix the view will refer to
 * @property row the row of the [matrix] this view will refer to
 * @constructor Create empty Row view
 * @throws IndexOutOfBoundsException if the [row] argument is not valid (less than `0` or greater than the last
 * index in the matrix)
 */
class RowView(
    private val matrix: Array<DoubleArray>,
    private val row: Int
) {

    init {
        if(row < 0 || row > countMatrixRows(matrix)) {
            throw IndexOutOfBoundsException("row index $row is not valid for matrix with rows ${countMatrixRows(matrix)}")
        }
    }

    operator fun get(column: Int): Double {
        return matrix[row][column]
    }

    operator fun set(column: Int, value: Double) {
        matrix[row][column] = value
    }

    /**
     * Returns the size of the row the view refers to
     *
     * @return the size of the row the view refers to
     */
    fun getSize(): Int {
        return matrix[row].size
    }

    /**
     * Returns an array that contains all the elements of the row this view refer to.
     * Every change in the returning array **will not affect** the row in the matrix
     *
     * @return an array that contains all the elements of the row this view refer to
     */
    fun toArray(): DoubleArray {
        return matrix[row].copyOf()
    }

    override operator fun equals(other: Any?): Boolean {
        if(other == null)
            return false

        if(other is DoubleArray) {
            return matrix[row].contentEquals(other)
        }

        if(other is IntArray) {
            return matrix[row].contentEquals(other.map { it.toDouble() }.toDoubleArray())
        }

        if(other is RowView)
            return matrix[row].contentEquals(other.matrix[other.row])

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
        if(other == null)
            return false

        if(this == other)
            return true

        if(other is ColumnView) {
            return matrix[row].contentEquals(other.toArray())
        }

        return false
    }
}