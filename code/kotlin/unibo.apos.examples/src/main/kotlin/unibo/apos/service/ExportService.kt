package unibo.apos.service

import unibo.apos.model.WorkspaceExportInfo

interface ExportService {

    fun createWorkspace(): String
    fun saveExports(workspaceExportInfo: WorkspaceExportInfo, customFileName: String?): String
}