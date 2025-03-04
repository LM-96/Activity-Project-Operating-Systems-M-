package unibo.apos.model

data class WorkspaceExportInfo (
    val workspaceId: String,
    val exportInfos: Collection<ExportInfo>,
)