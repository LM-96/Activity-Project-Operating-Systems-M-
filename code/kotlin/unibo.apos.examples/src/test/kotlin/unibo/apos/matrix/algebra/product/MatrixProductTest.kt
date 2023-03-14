package unibo.apos.matrix.algebra.product

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import unibo.apos.matrix.Matrix
import unibo.apos.matrix.createMatrix
import unibo.apos.matrix.row

class MatrixProductTest {

    companion object {
        val EXECUTORS = listOf<MatrixProductExecutor>(SeqIJKMatrixProductExecutor())

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

    @Test
    fun testMatAXMatB_isMatC() {
        EXECUTORS.forEach {
            assertArrayEquals(MAT_C, it.multiply(MAT_A, MAT_B))
        }
    }

}