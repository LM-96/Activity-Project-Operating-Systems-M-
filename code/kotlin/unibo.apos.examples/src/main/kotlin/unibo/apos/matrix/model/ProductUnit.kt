package unibo.apos.matrix.model

data class ProductUnit (
    val row: IntArray,
    val col: IntArray,
    val pointer: CellPointer,
) {
    companion object {
        val EMPTY = ProductUnit(intArrayOf(), intArrayOf(), CellPointer.EMPTY)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProductUnit

        if (!row.contentEquals(other.row)) return false
        if (!col.contentEquals(other.col)) return false
        if (pointer != other.pointer) return false

        return true
    }

    override fun hashCode(): Int {
        var result = row.contentHashCode()
        result = 31 * result + col.contentHashCode()
        result = 31 * result + pointer.hashCode()
        return result
    }
}