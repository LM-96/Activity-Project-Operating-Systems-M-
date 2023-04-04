package unibo.apos.matrix.algebra.product

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import unibo.apos.matrix.Matrix
import unibo.apos.matrix.createMatrix
import unibo.apos.matrix.row
import java.util.stream.Stream


class MatrixProductTest {

    companion object {

        @JvmStatic
        fun providedExecutors(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(SeqIJKMatrixProductExecutor()),
                Arguments.of(SeqIKJMatrixProductExecutor()),
                Arguments.of(ParallelChannelGuidedIJKMatrixProductExecutor())
            )
        }

        val MAT_A: Matrix = createMatrix {
            row[1.0, 2.0, 3.0]
            row[4.0, 5.0, 6.0]
        }

        val MAT_B: Matrix = createMatrix {
            row[10.0, 11.0]
            row[20.0, 21.0]
            row[30.0, 31.0]
        }

        val MAT_C: Matrix = createMatrix {
            row[140.0, 146.0]
            row[320.0, 335.0]
        }
    }

    @ParameterizedTest
    @MethodSource("providedExecutors")
    fun testMatAXMatB_isMatC(productExecutor: MatrixProductExecutor) {
        assertArrayEquals(MAT_C, productExecutor.multiply(MAT_A, MAT_B))
    }

}