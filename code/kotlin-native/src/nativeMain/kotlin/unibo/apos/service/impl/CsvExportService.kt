import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.io.Buffer
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString
import unibo.apos.model.AposHeaders
import unibo.apos.model.WorkspaceExportInfo
import unibo.apos.service.ExportService
import kotlin.random.Random

class CsvExportService : ExportService {

    override fun createWorkspace(): String {
        return generateUUID()
    }

    override fun saveExports(workspaceExportInfo: WorkspaceExportInfo, customFileName: String?): String {
        val fileName = customFileName ?: generateFileName()
        val filePath = Path(fileName)

        try {
            val buffer = Buffer()
            if (!SystemFileSystem.exists(filePath)) {
                buffer.writeString(AposHeaders.ALL.joinToString(",") + "\n")
            }

            val csvContent = workspaceExportInfo.exportInfos.joinToString("\n") { result ->
                listOf(
                    workspaceExportInfo.workspaceId,
                    result.size,
                    result.coroutines,
                    result.mode,
                    result.executionTimeMillis
                ).joinToString(",")
            }

            buffer.writeString(csvContent)
            SystemFileSystem.sink(filePath, append = true).use {
                it.write(buffer, buffer.size)
            }
        } catch (e: Exception) {
            throw RuntimeException("Failed to write to CSV file: ${e.message}", e)
        }

        return fileName
    }

    private fun generateUUID(): String {
        // Simple UUID generator for Kotlin/Native
        return List(4) { Random.nextInt(0, 65535).toString(16).padStart(4, '0') }.joinToString("-")
    }

    private fun generateFileName(): String {
        val currentTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val timestamp = currentTime.toString().replace(":", "").replace("-", "").replace("T", "-")
        return "apos-$timestamp.csv"
    }
}
