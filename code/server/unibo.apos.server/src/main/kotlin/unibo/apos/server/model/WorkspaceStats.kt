package unibo.apos.server.model

data class WorkspaceStats(
    val workspaceExportInfo: WorkspaceExportInfo,
    val minExecutionTimeMillis: Long,
    val maxExecutionTimeMillis: Long,
    val avgExecutionTimeMillis: Long,
    val size: Int,
    val workers: Int,
    val mode: String
)