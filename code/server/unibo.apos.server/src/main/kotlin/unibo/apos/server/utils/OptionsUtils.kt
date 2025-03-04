package unibo.apos.server.utils

import unibo.apos.server.model.AvailableColumn
import unibo.apos.server.model.WorkspaceMode

object OptionsUtils {
    var loggingEnabled = false
    var xColumn = AvailableColumn.SIZE
    var yColumn = AvailableColumn.AVG_TIME
    var selectedModes = WorkspaceMode.entries.toList()
}