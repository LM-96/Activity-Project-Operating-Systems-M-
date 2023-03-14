package unibo.apos.matrix.primitive

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import unibo.apos.matrix.*

class DoubleMatricesTest {

    companion object {
        val DATA: Array<DoubleArray> = arrayOf(
            doubleArrayOf(1.0, 2.0),
            doubleArrayOf(3.0, 4.0, 5.0, 6.0),
            doubleArrayOf(7.0)
        )
    }

    @Test
    fun testCreateEmptyMatrix() {
        val matrix: Array<DoubleArray> = createMatrix(2, 3)
        assertArrayEquals(Array(2){ doubleArrayOf(0.0, 0.0, 0.0) }, matrix)
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
    fun testCreateMatrixFromData() {
        val expected: Array<DoubleArray> = arrayOf(
            doubleArrayOf(1.0, 2.0, 0.0, 0.0),
            doubleArrayOf(3.0, 4.0, 5.0, 6.0),
            doubleArrayOf(7.0, 0.0, 0.0, 0.0)
        )

        val matrix: Array<DoubleArray> = createMatrix(DATA)

        assertArrayEquals(expected, matrix)
    }

    @Test
    fun testCreateMatrixFromDataExtensionFunction() {
        val expected: Array<DoubleArray> = arrayOf(
            doubleArrayOf(1.0, 2.0, 0.0, 0.0),
            doubleArrayOf(3.0, 4.0, 5.0, 6.0),
            doubleArrayOf(7.0, 0.0, 0.0, 0.0)
        )

        val matrix: Array<DoubleArray> = DATA.toMatrix()

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
        val matrix = reshapedMatrix(DATA, 4, 5)
        assertArrayEquals(expected, matrix)
    }

    @Test
    fun testReshapedWithTruncate() {
        val expected: Array<DoubleArray> = arrayOf(
            doubleArrayOf(1.0, 2.0),
            doubleArrayOf(3.0, 4.0)
        )
        val matrix = reshapedMatrix(DATA, 2, 2)
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
        val matrix = reshapedMatrix(expected, expected.size, expected[0].size)
        assertArrayEquals(expected, matrix)
    }

    @Test
    fun testReshapedWithFillExtensionFunction() {
        val expected: Array<DoubleArray> = arrayOf(
            doubleArrayOf(1.0, 2.0, 0.0, 0.0, 0.0),
            doubleArrayOf(3.0, 4.0, 5.0, 6.0, 0.0),
            doubleArrayOf(7.0, 0.0, 0.0, 0.0, 0.0),
            doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0)
        )
        val matrix = DATA.reshapedMatrix(4, 5)
        assertArrayEquals(expected, matrix)
    }

    @Test
    fun testReshapedWithTruncateExtensionFunction() {
        val expected: Array<DoubleArray> = arrayOf(
            doubleArrayOf(1.0, 2.0),
            doubleArrayOf(3.0, 4.0)
        )
        val matrix = DATA.reshapedMatrix(2, 2)
        assertArrayEquals(expected, matrix)
    }

    @Test
    fun testReshapeUntouchedExtensionFunction() {
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
    fun testCountMatrixRows() {
        assertEquals(0, countMatrixRows(arrayOf()))
        assertEquals(3, countMatrixRows(DATA))
    }

    @Test
    fun testCountMatrixRowsExtensionFunction() {
        assertEquals(0, arrayOf<DoubleArray>().countMatrixRows())
        assertEquals(3, DATA.countMatrixRows())
    }

    @Test
    fun testCountMatrixColumns() {
        assertEquals(0, countMatrixColumns(arrayOf()))
        assertEquals(4, countMatrixColumns(createMatrix(DATA)))
    }

    @Test
    fun testCountMatrixColumnsExtensionFunction() {
        assertEquals(0, arrayOf<DoubleArray>().countMatrixColumns())
        assertEquals(4, DATA.countMatrixColumns())
    }

}