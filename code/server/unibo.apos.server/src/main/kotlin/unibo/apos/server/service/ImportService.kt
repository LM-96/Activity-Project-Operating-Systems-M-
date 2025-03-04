package unibo.apos.server.service

import unibo.apos.server.model.WorkspaceExportInfo

interface ImportService {
    fun parseWorkspaceExportInfos(filePathString: String): List<WorkspaceExportInfo>
}