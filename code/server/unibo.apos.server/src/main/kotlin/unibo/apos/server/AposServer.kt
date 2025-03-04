package unibo.apos.server

import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import unibo.apos.server.model.WorkspaceExportInfo
import unibo.apos.server.model.WorkspaceStats
import java.util.concurrent.Callable
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.inject
import org.koin.ksp.generated.module
import unibo.apos.server.model.AvailableColumn
import unibo.apos.server.model.WorkspaceMode
import unibo.apos.server.service.ImportService
import unibo.apos.server.service.PlotService
import unibo.apos.server.utils.OptionsUtils
import kotlin.system.exitProcess

@Command(
    name = "apos-server",
    mixinStandardHelpOptions = true,
    version = ["1.0"],
    description = ["Processes CSV data and generates statistical plots for workspace execution times."]
)
class AposServer : Callable<Int> {
    private val log = KotlinLogging.logger {}

    @Option(names = ["-l", "--log"], description = ["Enable detailed logging"], defaultValue = "false")
    private var enableLogging: Boolean = false

    @Parameters(index = "0", description = ["CSV file to process"])
    private lateinit var csvFilePath: String

    @Option(names = ["-o", "--output"], description = ["Output PDF file path"], defaultValue = "workspace_stats.pdf")
    private lateinit var outputFilePath: String

    @Option(names = ["--x"], description = ["CSV column name for the X-axis" ], defaultValue = "size")
    private lateinit var xColumn: AvailableColumn

    @Option(names = ["--y"], description = ["CSV column name for the Y-axis"], defaultValue = "timeMillis")
    private lateinit var yColumn: AvailableColumn

    @Option(names = ["-m",  "--modes"], description = ["Comma-separated list of modes to display"], split = ",")
    private var selectedModes: List<WorkspaceMode> = WorkspaceMode.entries.distinct().toList()

    private val importService: ImportService by inject(ImportService::class.java)
    private val plotService: PlotService by inject(PlotService::class.java)

    override fun call(): Int {
        try {
            setupInjection()
            setupOptions()

            val workspaceExportInfos = importService.parseWorkspaceExportInfos(csvFilePath)
            withLog { info { "Found ${workspaceExportInfos.size} workspaces in the CSV" } }

            withLog { info { "Generating statistics for workspaces" } }
            val workspaceStats = generateStats(workspaceExportInfos)

            withLog { info { "Generating plot and saving to $outputFilePath" } }
            plotService.generatePlot(workspaceStats, outputFilePath)

            withLog { info { "Plot saved successfully to $outputFilePath" } }
            return 0
        } catch (e: Exception) {
            log.error(e) { "Error processing CSV data: ${e.message}" }
            return 1
        }
    }

    private fun generateStats(workspaceExportInfos: List<WorkspaceExportInfo>): List<WorkspaceStats> {
        return workspaceExportInfos.map { workspaceExportInfo ->
            val executionTimes = workspaceExportInfo.exportInfos.map { it.executionTimeMillis }

            val minTime = executionTimes.minOrNull() ?: 0
            val maxTime = executionTimes.maxOrNull() ?: 0
            val avgTime = if (executionTimes.isNotEmpty()) executionTimes.sum() / executionTimes.size else 0
            val sizes = workspaceExportInfo.exportInfos.map { it.size }.distinct()
            if (sizes.size != 1) withLog { error { "Invalid workspace with multiple sizes [workspaceId: "+
                    workspaceExportInfo.workspaceId + ", sizes: " + sizes + "]" } }
            val workers = workspaceExportInfo.exportInfos.map { it.coroutines }.distinct()
            if (workers.size != 1) withLog { error { "Invalid workspace with multiple coroutines [workspaceId: " +
                    workspaceExportInfo.workspaceId + ", workers: " + workers + "]" } }
            val modes = workspaceExportInfo.exportInfos.map { it.mode }.distinct()
            if (modes.size != 1) withLog { error { "Invalid workspace with multiple modes [workspaceId: " +
                    workspaceExportInfo.workspaceId + ", modes: " + modes + "]" } }

            withLog {
                debug {
                    "Stats for workspace ${workspaceExportInfo.workspaceId}: " +
                            "min=$minTime ms, max=$maxTime ms, avg=$avgTime ms"
                }
            }

            WorkspaceStats(
                workspaceExportInfo = workspaceExportInfo,
                minExecutionTimeMillis = minTime,
                maxExecutionTimeMillis = maxTime,
                avgExecutionTimeMillis = avgTime,
                size = sizes.first(),
                workers = workers.first(),
                mode = modes.first()
            )
        }
    }

    private fun setupInjection() {
        startKoin() {
            if (enableLogging) {
                printLogger()
            }
            modules(AposKoinModule().module)
        }
    }

    private fun setupOptions() {
        OptionsUtils.loggingEnabled = enableLogging
        OptionsUtils.xColumn = xColumn
        OptionsUtils.yColumn = yColumn
        OptionsUtils.selectedModes = selectedModes
    }

    private fun withLog(block: KLogger.() -> Unit) {
        if (enableLogging) {
            log.block()
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val exitCode = CommandLine(AposServer()).execute(*args)
            exitProcess(exitCode)
        }
    }
}