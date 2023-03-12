package unibo.apos.matrix.math.product

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import unibo.apos.matrix.types.createIntMatrix
import java.lang.AssertionError

class TestIntProductExecutor {

    private val EXECUTORS = arrayListOf(
        SeqIKJIntMatrixProductExecutor(),
        SeqIJKIntMatrixProductExecutor(),
    )

    private val matA = createIntMatrix {
        row(1, 2, 3)
        row(4, 5, 6)
    }

    private val matB = createIntMatrix {
        row(10, 11)
        row(20, 21)
        row(30, 31)
    }

    private val matC = createIntMatrix {
        row(140, 146)
        row(320, 335)
    }
    
    @Test
    fun testRightProduct() {
        for(executor in EXECUTORS) {
            try {
                assertArrayEquals(matC, executor.multiply(matA, matB))
            } catch (a: AssertionError) {
                error("Error in executor ${executor.javaClass.simpleName}")
                throw a
            }
        }
    }

}