package unibo.apos.matrix

import unibo.apos.matrix.types.createIntMatrix
import unibo.apos.matrix.types.print

fun main() {
    val matrix = createIntMatrix {
        row(1, 2, 3)
        row(4, 5, 6)
        row(7, 8, 9)
    }
    matrix.print()
}