package unibo.apos.model

data class ExportInfo(
    val size: Int,
    val coroutines: Int,
    val mode: String,
    val executionTimeMillis: Long
)