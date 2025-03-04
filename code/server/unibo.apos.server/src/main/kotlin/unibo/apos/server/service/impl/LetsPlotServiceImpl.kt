package unibo.apos.server.service.impl

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.letsPlot.export.ggsave
import org.jetbrains.letsPlot.geom.geomLine
import org.jetbrains.letsPlot.ggplot
import org.jetbrains.letsPlot.intern.Plot
import org.jetbrains.letsPlot.label.labs
import org.jetbrains.letsPlot.scale.scaleColorManual
import org.jetbrains.letsPlot.scale.scaleXContinuous
import org.jetbrains.letsPlot.scale.scaleYContinuous
import org.koin.core.annotation.Single
import unibo.apos.server.model.AvailableColumn
import unibo.apos.server.model.WorkspaceMode
import unibo.apos.server.model.WorkspaceStats
import unibo.apos.server.service.PlotService
import unibo.apos.server.utils.OptionsUtils
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

@Single
class LetsPlotServiceImpl: PlotService {
    private val log = KotlinLogging.logger {}

    private val availableColors = listOf(
        "#1f77b4", "#ff7f0e", "#2ca02c", "#d62728", "#9467bd",
        "#8c564b", "#e377c2", "#7f7f7f", "#bcbd22", "#17becf"
    )

    private val columnToValue = mapOf<AvailableColumn, (WorkspaceStats) -> Number>(
        AvailableColumn.SIZE to WorkspaceStats::size,
        AvailableColumn.MIN_TIME to WorkspaceStats::minExecutionTimeMillis,
        AvailableColumn.MAX_TIME to WorkspaceStats::maxExecutionTimeMillis,
        AvailableColumn.AVG_TIME to WorkspaceStats::avgExecutionTimeMillis,
        AvailableColumn.WORKERS to WorkspaceStats::workers
    )

    private val columnToLabel = mapOf(
        AvailableColumn.SIZE to "Size",
        AvailableColumn.MIN_TIME to "Min execution time (ms)",
        AvailableColumn.MAX_TIME to "Max execution time (ms)",
        AvailableColumn.AVG_TIME to "Average execution time (ms)",
        AvailableColumn.WORKERS to "Workers"
    )

    override fun generatePlot(workspaceStats: List<WorkspaceStats>, outputFilePathString: String) {
        if (workspaceStats.isEmpty()) {
            withLog { warn { "No workspace stats available for plotting" } }
            return
        }

        val plotData = prepareDataForPlotting(workspaceStats)
        if (plotData.isEmpty()) {
            withLog { warn { "No data series available for plotting" } }
            return
        }

        val colorMapping = createColorMapping(plotData.keys)
        val plot = createPlotWithLayers(plotData)
        if (plot == null) {
            withLog { error { "No valid data layers could be created for the plot" } }
            return
        }

        val finalPlot = addLabelsAndScales(plot, colorMapping)
        val outputFile = Path(outputFilePathString).toAbsolutePath()
        ensureOutputDirectoryExists(outputFile)
        withLog { info { "Saving plot to $outputFilePathString" } }
        ggsave(plot = finalPlot, filename = outputFile.fileName.toString(), path = outputFile.absolute().parent.toString())
    }

    private fun prepareDataForPlotting(workspaceStatsCollection: List<WorkspaceStats>): Map<String, List<Map<String, Any>>> {
        val plotData = mutableMapOf<String, MutableList<Map<String, Any>>>()

        val xGetter: (WorkspaceStats) -> Number = columnToValue[OptionsUtils.xColumn] as ((WorkspaceStats) -> Number)
        val yGetter = columnToValue[OptionsUtils.yColumn] as ((WorkspaceStats) -> Number)
        workspaceStatsCollection.forEach { workspaceStats ->
            val mode = workspaceStats.mode
            if (OptionsUtils.selectedModes.contains(WorkspaceMode.fromCode(mode))) {
                if (!plotData.containsKey(mode)) {
                    plotData[mode] = mutableListOf()
                    withLog { debug { "Creating new data series for mode: $mode" } }
                }

                plotData[mode]?.add(
                    mapOf(
                        OptionsUtils.xColumn.code to xGetter(workspaceStats),
                        OptionsUtils.yColumn.code to yGetter(workspaceStats),
                        "workspace" to workspaceStats.workspaceExportInfo.workspaceId
                    )
                )
            }
        }

        withLog { info { "Created data for ${plotData.size} different modes" } }
        return plotData
    }

    private fun createColorMapping(modes: Set<String>): Map<String, String> {
        val colorMapping = mutableMapOf<String, String>()
        modes.forEachIndexed { index, mode ->
            colorMapping[mode] = availableColors[index % availableColors.size]
        }
        return colorMapping
    }

    private fun createPlotWithLayers(plotData: Map<String, List<Map<String, Any>>>): Plot? {
        var plot = ggplot()
        var hasLayers = false

        plotData.forEach { (mode, data) ->
            if (data.isEmpty()) {
                withLog { debug { "Skipping empty data series for mode: $mode" } }
                return@forEach
            }

            val sortedData = data.sortedBy { (it[OptionsUtils.xColumn.code] as Number).toLong() }
            val xValues = sortedData.map { (it[OptionsUtils.xColumn.code] as Number).toLong() }
            val yValues = sortedData.map { (it[OptionsUtils.yColumn.code] as Number).toLong() }

            if (xValues.isEmpty() || yValues.isEmpty()) {
                withLog { debug { "Skipping mode '$mode' with empty data points" } }
                return@forEach
            }

            withLog { debug { "Adding line for mode '$mode' with ${xValues.size} data points" } }

            val dataWithMode = mapOf(
                OptionsUtils.xColumn.code to xValues,
                OptionsUtils.yColumn.code to yValues,
                "mode" to List(xValues.size) { mode }
            )

            plot = plot.plus(
                geomLine(
                    data = dataWithMode,
                    size = 1.0
                ) {
                    x = OptionsUtils.xColumn.code
                    y = OptionsUtils.yColumn.code
                    color = "mode" // Ensure color is mapped to "mode"
                }
            )
            hasLayers = true
        }

        return if (hasLayers) plot else null
    }

    private fun addLabelsAndScales(plot: Plot, colorMapping: Map<String, String>): Plot {
        return plot.plus(
            labs(
                title = getTitle(),
                x = columnToLabel[OptionsUtils.xColumn],
                y = columnToLabel[OptionsUtils.yColumn]
            )
        ).plus(
            scaleXContinuous(name = columnToLabel[OptionsUtils.xColumn])
        ).plus(
            scaleYContinuous(name = columnToLabel[OptionsUtils.yColumn])
        ).plus(
            scaleColorManual(name = "Mode", values = colorMapping) // Define the legend
        )
    }

    private fun ensureOutputDirectoryExists(path: Path) {
        val absolutePath = if (path.isAbsolute) path else path.toAbsolutePath().normalize()
        val parent = absolutePath.parent
        if (!parent.exists() || !parent.isDirectory()) {
            Files.createDirectories(path.parent)
        }
    }

    private fun getTitle(): String {
        return columnToLabel[OptionsUtils.yColumn] + " by " + columnToLabel[OptionsUtils.xColumn]
    }

    private fun withLog(block: KLogger.() -> Unit) {
        if (OptionsUtils.loggingEnabled) {
            log.block()
        }
    }
}