package unibo.apos.server.service

import unibo.apos.server.model.WorkspaceStats

interface PlotService {
    fun generatePlot(workspaceStats: List<WorkspaceStats>, outputFilePathString: String)
}