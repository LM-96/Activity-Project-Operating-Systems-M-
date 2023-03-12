package unibo.apos.matrix.types


import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import unibo.apos.matrix.validation.exceptions.InvalidRowsSizeException

internal class IntMatrixTypeTest {

    private val mat0 = arrayOf(intArrayOf(0, 0, 0), intArrayOf(0, 0, 0))
    private val mat1 = arrayOf(intArrayOf(1, 2, 3), intArrayOf(4, 5, 6))

    @Test
    fun testCreateEmptyIntMatrix() {
        val rows = 2
        val columns = 3
        val matrix = createIntMatrix(rows, columns)

        assertEquals(2, matrix.size)
        assertEquals(3, matrix[0].size)
        assertEquals(2, matrix.getNumberOfRows())
        assertEquals(3, matrix.getNumberOfColumns())
        assertArrayEquals(mat0, matrix)
    }

    @Test
    fun testCreateIntMatrixUsingIndices() {
        val matrix = createIntMatrix(2, 3) { row, col -> row * 3 + col + 1 }
        assertArrayEquals(mat1, matrix)
        assertEquals(2, matrix.getNumberOfRows())
        assertEquals(3, matrix.getNumberOfColumns())
    }

    @Test
    fun testCreateIntMatrixUsingDSL() {
        val matrix = createIntMatrix {
            row(1, 2, 3)
            row(4, 5, 6)
        }
        assertArrayEquals(mat1, matrix)
        assertEquals(2, matrix.getNumberOfRows())
        assertEquals(3, matrix.getNumberOfColumns())
    }

    @Test
    fun testCreateMatrixWithWrongColumn() {
        val exception = assertThrows(InvalidRowsSizeException::class.java) {
            createIntMatrix {
                row(1, 2, 3)
                row(4, 5, 6)
                row(7, 8)
            }
        }

        assertEquals(3, exception.expectedSize)
        assertEquals(2, exception.row.size)
    }

}