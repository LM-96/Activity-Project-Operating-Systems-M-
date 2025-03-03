package unibo.apos.matrix.model

data class CellPointer(
    val row: Int,
    val col: Int,
) {
    companion object {
        val EMPTY = CellPointer(-1, -1);
    }
}
