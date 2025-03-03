package unibo.apos.service.impl

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter
import org.koin.core.annotation.Single
import unibo.apos.model.AposHeaders
import unibo.apos.model.WorkspaceExportInfo
import unibo.apos.service.ExportService
import java.io.FileReader
import java.io.FileWriter
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.writer

@Single
class CsvExportService : ExportService {

    override fun createWorkspace(): String {
        return UUID.randomUUID().toString()
    }

    override fun saveExports(workspaceExportInfo: WorkspaceExportInfo, customFileName: String?): String {
        val fileName = customFileName ?: generateFileName()
        val filePath = Path(fileName)
        val isNewFile = !filePath.exists()

        try {
            filePath.writer(options = arrayOf(StandardOpenOption.CREATE, StandardOpenOption.APPEND))
                .use { fileWriter ->
                    val csvFormat = if (isNewFile) {
                        CSVFormat.DEFAULT.builder()
                            .setHeader(*AposHeaders.ALL)
                            .get()
                    } else {
                        CSVFormat.DEFAULT
                    }

                    CSVPrinter(fileWriter, csvFormat).use { printer ->
                        for (result in workspaceExportInfo.exportInfos) {
                            printer.printRecord(
                                workspaceExportInfo.workspaceId,
                                result.size,
                                result.coroutines,
                                result.mode,
                                result.executionTimeMillis
                            )
                        }
                    }
                }
        } catch (e: Exception) {
            throw RuntimeException("Failed to write to CSV file: ${e.message}", e)
        }

        return fileName
    }

    private fun generateFileName(): String {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
        return "apos-$timestamp.csv"
    }
}