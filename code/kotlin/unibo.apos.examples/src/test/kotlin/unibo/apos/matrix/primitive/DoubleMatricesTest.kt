package unibo.apos.matrix.primitive

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import unibo.apos.matrix.*

class DoubleMatricesTest {

    companion object {
        val MAT1: Matrix = arrayOf(
            doubleArrayOf(1.0, 2.0, 3.0, 4.0),
            doubleArrayOf(5.0, 6.0, 7.0, 8.0),
            doubleArrayOf(9.0, 10.0, 11.0, 12.0)
        )

        val NORMAL_ARRAY: Array<DoubleArray> = arrayOf(
            doubleArrayOf(1.0, 2.0),
            doubleArrayOf(3.0, 4.0, 5.0, 6.0),
            doubleArrayOf(7.0)
        )
    }

    @Test
    fun testCreateEmptyMatrixWithRowsAndColumns() {
        val expected: Array<DoubleArray> = Array(2) { doubleArrayOf(0.0, 0.0, 0.0) }
        val matrix: Array<DoubleArray> = createMatrix(2, 3)
        assertArrayEquals(expected, matrix)
    }

    @Test
    fun testCreateMatrixWithGenerator() {
        val matrix: Array<DoubleArray> = createMatrix(2, 3){ row, col -> ((row*3) + col).toDouble() }
        val expected = arrayOf(
            doubleArrayOf(0.0, 1.0, 2.0),
            doubleArrayOf(3.0, 4.0, 5.0)
        )
        assertArrayEquals(expected, matrix)
    }

    @Test
    fun testNormalArrayToMatrix() {
        val expected: Array<DoubleArray> = arrayOf(
            doubleArrayOf(1.0, 2.0, 0.0, 0.0),
            doubleArrayOf(3.0, 4.0, 5.0, 6.0),
            doubleArrayOf(7.0, 0.0, 0.0, 0.0)
        )

        val matrix: Array<DoubleArray> = NORMAL_ARRAY.toMatrix()

        assertArrayEquals(expected, matrix)
    }

    @Test
    fun testReshapedWithFill() {
        val expected: Array<DoubleArray> = arrayOf(
            doubleArrayOf(1.0, 2.0, 0.0, 0.0, 0.0),
            doubleArrayOf(3.0, 4.0, 5.0, 6.0, 0.0),
            doubleArrayOf(7.0, 0.0, 0.0, 0.0, 0.0),
            doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0)
        )
        val matrix = NORMAL_ARRAY.reshapedMatrix(4, 5)
        assertArrayEquals(expected, matrix)
    }

    @Test
    fun testReshapedWithTruncate() {
        val expected: Array<DoubleArray> = arrayOf(
            doubleArrayOf(1.0, 2.0),
            doubleArrayOf(3.0, 4.0)
        )
        val matrix = NORMAL_ARRAY.reshapedMatrix(2, 2)
        assertArrayEquals(expected, matrix)
    }

    @Test
    fun testReshapeUntouched() {
        val expected = arrayOf(
            doubleArrayOf(1.0, 2.0, 0.0, 0.0, 0.0),
            doubleArrayOf(3.0, 4.0, 5.0, 6.0, 0.0),
            doubleArrayOf(7.0, 0.0, 0.0, 0.0, 0.0),
            doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0)
        )

        val matrix = expected.reshapedMatrix(expected.size, expected[0].size)
        assertArrayEquals(expected, matrix)
    }

    @Test
    fun testCountMatrixRow_zeroRowsMatrix() {
        assertEquals(0, arrayOf<DoubleArray>().countMatrixRows())
    }

    @Test
    fun testCountMatrixRow_mat1() {
        assertEquals(3, MAT1.countMatrixRows())
    }

    @Test
    fun testCountMatrixColumns_zeroRowsMatrix() {
        assertEquals(0, arrayOf<DoubleArray>().countMatrixColumns())
    }

    @Test
    fun testCountMatrixColumns_mat1() {
        assertEquals(4, MAT1.countMatrixColumns())
    }

    @Test
    fun testCountMatrixColumn_normalArrayWithoutMatrixShape_thenExceptionIsThrown() {
        assertEquals(2, NORMAL_ARRAY.countMatrixColumns())
    }

    @Test
    fun testValidateMatrixShapeOrThrowWithMatrix() {
        assertDoesNotThrow {
            MAT1.validateMatrixShapeOrThrows()
        }
    }

    @Test
    fun testValidateMatrixShapeOrThrowWithNotShapedArray_thenThrowException() {
        assertThrows<IllegalArgumentException> {
            NORMAL_ARRAY.validateMatrixShapeOrThrows()
        }
    }

    @Test
    fun testValidateMatrixShapeOrThrowWithMatrix_zeroRows() {
        assertDoesNotThrow {
            arrayOf<DoubleArray>().validateMatrixShapeOrThrows()
        }
    }

    @Test
    fun testHasMatrixShapeWithMatrix() {
        assertTrue(MAT1.hasMatrixShape())
    }

    @Test
    fun testHasMatrixShapeOrThrowWithNotShapedArray_thenThrowException() {
        assertFalse(NORMAL_ARRAY.hasMatrixShape())
    }

    @Test
    fun testHasMatrixShapeOrThrowWithMatrix_zeroRows() {
        assertTrue( arrayOf<DoubleArray>().hasMatrixShape())
    }

}