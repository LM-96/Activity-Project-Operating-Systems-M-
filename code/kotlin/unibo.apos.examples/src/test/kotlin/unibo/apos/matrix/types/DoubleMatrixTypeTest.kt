package unibo.apos.matrix.types

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import unibo.apos.matrix.types.createDoubleMatrix
import unibo.apos.matrix.validation.exceptions.InvalidRowsSizeException


class DoubleMatrixTypeTest {

    private val mat0 = arrayOf(doubleArrayOf(0.0, 0.0, 0.0), doubleArrayOf(0.0, 0.0, 0.0))
    private val mat1 = arrayOf(doubleArrayOf(1.1, 2.2, 3.3), doubleArrayOf(4.4, 5.5, 6.6))

    @Test
    fun testCreateEmptyIntMatrix() {
        val rows = 2
        val columns = 3
        val matrix = createDoubleMatrix(rows, columns)

        assertEquals(2, matrix.size)
        assertEquals(3, matrix[0].size)
        assertEquals(2, matrix.getNumberOfRows())
        assertEquals(3, matrix.getNumberOfColumns())
        assertArrayEquals(mat0, matrix)
    }

    @Test
    fun testCreateIntMatrixUsingIndices() {
        val matrix = createDoubleMatrix(2, 3) { row, col ->
            val value = row * 3 + col + 1
            return@createDoubleMatrix "$value.$value".toDouble()
        }
        assertArrayEquals(mat1, matrix)
        assertEquals(2, matrix.getNumberOfRows())
        assertEquals(3, matrix.getNumberOfColumns())
    }

    @Test
    fun testCreateIntMatrixUsingDSL() {
        val matrix = createDoubleMatrix {
            row(1.1, 2.2, 3.3)
            row(4.4, 5.5, 6.6)
        }
        assertArrayEquals(mat1, matrix)
        assertEquals(2, matrix.getNumberOfRows())
        assertEquals(3, matrix.getNumberOfColumns())
    }

    @Test
    fun testCreateMatrixWithWrongColumn() {
        val exception = assertThrows(InvalidRowsSizeException::class.java) {
            createDoubleMatrix {
                row(1.1, 2.2, 3.3)
                row(4.4, 5.5, 6.6)
                row(7.7, 8.8)
            }
        }
        assertEquals(3, exception.expectedSize)
        assertEquals(2, exception.row.size)
    }

}