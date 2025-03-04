package unibo.apos.server.model

data class WorkspaceExportInfo (
    val workspaceId: String,
    val exportInfos: Collection<ExportInfo>,
)