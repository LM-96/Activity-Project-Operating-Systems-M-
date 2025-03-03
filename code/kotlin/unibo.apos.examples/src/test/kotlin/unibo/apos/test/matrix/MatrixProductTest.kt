package unibo.apos.test.matrix

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.coroutines.runBlocking
import org.ejml.simple.SimpleMatrix
import org.junit.jupiter.api.DisplayName
import unibo.apos.matrix.product.MatrixProduct
import unibo.apos.matrix.product.impl.CoordinatorChanneledMatrixProductImpl
import unibo.apos.matrix.product.impl.FanChanneledMatrixProductImpl
import unibo.apos.matrix.product.impl.PureChanneledMatrixProductImpl
import kotlin.test.Test

@DisplayName("Matrix Product Tests")
class MatrixProductTest {

    private val matrixProductImpls = listOf(
        CoordinatorChanneledMatrixProductImpl(),
        FanChanneledMatrixProductImpl(),
        PureChanneledMatrixProductImpl()
    )

    @Test
    @DisplayName("Should return the right result when multiplying two 3x3 matrices with Channeled implementation")
    fun `should returnCorrectResult when multiplyingWith3x3Matrices`() {
        val matA = arrayOf(
            intArrayOf(1, 2, 3),
            intArrayOf(4, 5, 6),
            intArrayOf(7, 8, 9)
        )
        val matB = arrayOf(
            intArrayOf(9, 8, 7),
            intArrayOf(6, 5, 4),
            intArrayOf(3, 2, 1)
        )
        val expected = computeExpectedResult(matA, matB)

        test {
            val result = multiply(matA, matB, 4)
            assertThat(result.contentDeepToString()).isEqualTo(expected.contentDeepToString())
        }
    }

    @Test
    @DisplayName("Should return the right result when multiplying two 3x3 matrices with FanChanneled implementation")
    fun `should returnCorrectResult when multiplyingWith3x3MatricesUsingFanChanneled`() {
        val matA = arrayOf(
            intArrayOf(1, 2, 3),
            intArrayOf(4, 5, 6),
            intArrayOf(7, 8, 9)
        )
        val matB = arrayOf(
            intArrayOf(9, 8, 7),
            intArrayOf(6, 5, 4),
            intArrayOf(3, 2, 1)
        )
        val expected = computeExpectedResult(matA, matB)

        test {
            val result = multiply(matA, matB, 4)
            assertThat(result.contentDeepToString()).isEqualTo(expected.contentDeepToString())
        }
    }

    @Test
    @DisplayName("Should return the same matrix when multiplying by identity matrix")
    fun `should returnSameMatrix when multiplyingWithIdentityMatrix`() {
        val size = 5
        val identity = Array(size) { i -> IntArray(size) { j -> if (i == j) 1 else 0 } }
        val randomMatrix = createRandomMatrix(size)

        test {
            val result = multiply(randomMatrix, identity, 4)
            assertThat(result.contentDeepToString()).isEqualTo(randomMatrix.contentDeepToString())
        }
    }

    @Test
    @DisplayName("Should return zero matrix when multiplying by zero matrix")
    fun `should returnZeroMatrix when multiplyingWithZeroMatrix`() {
        val size = 4
        val zeroMatrix = Array(size) { IntArray(size) { 0 } }
        val randomMatrix = createRandomMatrix(size)

        test {
            val result = multiply(randomMatrix, zeroMatrix, 3)
            assertThat(result.contentDeepToString()).isEqualTo(zeroMatrix.contentDeepToString())
        }
    }

    @Test
    @DisplayName("Should return consistent results when using different worker counts")
    fun `should returnConsistentResults when usingDifferentWorkerCounts`() {
        val matA = arrayOf(
            intArrayOf(1, 2),
            intArrayOf(3, 4)
        )
        val matB = arrayOf(
            intArrayOf(5, 6),
            intArrayOf(7, 8)
        )
        val expected = computeExpectedResult(matA, matB)

        test {
            for (workers in 1..8) {
                val result = multiply(matA, matB, workers)
                assertThat(result.contentDeepToString()).isEqualTo(expected.contentDeepToString())
            }
        }
    }

    @Test
    @DisplayName("Should handle non-square matrices correctly")
    fun `should handleNonSquareMatrices when multiplyingCompatibleDimensions`() {
        val matA = arrayOf(
            intArrayOf(1, 2, 3),
            intArrayOf(4, 5, 6)
        )
        val matB = arrayOf(
            intArrayOf(7, 8),
            intArrayOf(9, 10),
            intArrayOf(11, 12)
        )
        val expected = computeExpectedResult(matA, matB)

        test {
            val result = multiply(matA, matB, 2)
            assertThat(result.contentDeepToString()).isEqualTo(expected.contentDeepToString())
        }
    }

    private fun createRandomMatrix(size: Int): Array<IntArray> {
        return Array(size) { IntArray(size) { (Math.random() * 10).toInt() } }
    }

    private fun computeExpectedResult(matA: Array<IntArray>, matB: Array<IntArray>): Array<IntArray> {
        val simpleMatA = toSimpleMatrix(matA)
        val simpleMatB = toSimpleMatrix(matB)
        val simpleMatC = simpleMatA.mult(simpleMatB)
        return toIntArrayMatrix(simpleMatC);
    }

    private fun test(block: suspend MatrixProduct.() -> Unit) {
        runBlocking {
            for (matrixProductImpl in matrixProductImpls) {
                matrixProductImpl.block()
            }
        }
    }

    private fun toIntArrayMatrix(simpleMatrix: SimpleMatrix): Array<IntArray> {
        return Array(simpleMatrix.numRows) { row ->
            IntArray(simpleMatrix.numCols) { col ->
                simpleMatrix[row, col].toInt()
            }
        }
    }

    private fun toSimpleMatrix(mat: Array<IntArray>): SimpleMatrix {
        return SimpleMatrix(mat.map { row ->
            row.map { cell ->
                cell.toDouble()
            }.toDoubleArray()
        }.toTypedArray())
    }
}