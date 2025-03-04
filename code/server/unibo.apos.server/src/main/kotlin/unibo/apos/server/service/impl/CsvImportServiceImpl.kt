package unibo.apos.server.service.impl

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import org.koin.core.annotation.Single
import unibo.apos.server.model.ExportInfo
import unibo.apos.server.model.WorkspaceExportInfo
import unibo.apos.server.service.ImportService
import unibo.apos.server.utils.OptionsUtils
import java.io.Reader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.reader

@Single
class CsvImportServiceImpl : ImportService {
    private val log = KotlinLogging.logger {}

    override fun parseWorkspaceExportInfos(filePathString: String): List<WorkspaceExportInfo> {
        val workspaceMap = mutableMapOf<String, MutableList<ExportInfo>>()
        val filePath = Path(filePathString).toAbsolutePath()
        withLog { info { "Parsing CSV file: $filePathString" } }

        filePath.reader().use { reader ->
            val parser = buildCsvParser(reader)

            for (record in parser) {
                val exportInfo = toExportInfo(record)
                val workspaceId = record.get("workspaceId")

                if (!workspaceMap.containsKey(workspaceId)) {
                    workspaceMap[workspaceId] = mutableListOf()
                    withLog { debug { "Found new workspace: $workspaceId" } }
                }
                workspaceMap[workspaceId]?.add(exportInfo)
            }
        }

        return workspaceMap.map { (workspaceId, exportInfos) ->
            withLog { debug { "Created WorkspaceExportInfo for $workspaceId with ${exportInfos.size} export infos" } }
            WorkspaceExportInfo(workspaceId, exportInfos)
        }
    }

    private fun buildCsvParser(reader: Reader): CSVParser {
        return CSVParser.parse(
            reader,
            CSVFormat.DEFAULT
                .builder()
                .setHeader()
                .setIgnoreHeaderCase(true)
                .setTrim(true)
                .get()
        )
    }

    private fun toExportInfo(record: CSVRecord): ExportInfo {
        val size = record.get("size").toInt()
        val coroutines = record.get("coroutine").toInt()
        val mode = record.get("mode")
        val timeMillis = record.get("timeMillis").toLong()

        return ExportInfo(size, coroutines, mode, timeMillis)
    }

    private fun withLog(block: KLogger.() -> Unit) {
        if (OptionsUtils.loggingEnabled) {
            log.block()
        }
    }
}