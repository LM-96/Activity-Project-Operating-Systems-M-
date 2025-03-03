package unibo.apos.matrix.product

interface MatrixProduct {
    suspend fun multiply(matA: Array<IntArray>, matB: Array<IntArray>, workers: Int): Array<IntArray>
}