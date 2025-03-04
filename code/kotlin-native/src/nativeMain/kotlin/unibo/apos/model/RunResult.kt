package unibo.apos.model

data class RunResult (
    val iteration: Int,
    val matA: Array<IntArray>,
    val matB: Array<IntArray>,
    val result: Array<IntArray>,
    val elapsedTimeMillis: Long
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RunResult) return false

        if (iteration != other.iteration) return false
        if (elapsedTimeMillis != other.elapsedTimeMillis) return false
        if (!matA.contentDeepEquals(other.matA)) return false
        if (!matB.contentDeepEquals(other.matB)) return false
        if (!result.contentDeepEquals(other.result)) return false

        return true
    }

    override fun hashCode(): Int {
        var result1 = iteration
        result1 = 31 * result1 + elapsedTimeMillis.hashCode()
        result1 = 31 * result1 + matA.contentDeepHashCode()
        result1 = 31 * result1 + matB.contentDeepHashCode()
        result1 = 31 * result1 + result.contentDeepHashCode()
        return result1
    }


}