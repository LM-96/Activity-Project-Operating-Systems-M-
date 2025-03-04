package unibo.apos


import CsvExportService
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.int
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import unibo.apos.matrix.utils.MatrixUtils
import unibo.apos.matrix.product.ConcurrencyType
import unibo.apos.matrix.product.MatrixProduct
import unibo.apos.matrix.product.MatrixProductFactory
import unibo.apos.model.ExportInfo
import unibo.apos.model.RunResult
import unibo.apos.model.WorkspaceExportInfo
import kotlin.time.measureTime

class KAposMatrixApp : CliktCommand(
    name = "unibo/apos"
) {
    private val log = KotlinLogging.logger {}

    private val size: Int by option("-s", "--size", help = "Size of the matrices (NxN)")
        .int().default(3)

    private val coroutines: Int by option("-c", "--coroutines", help = "Number of coroutines to use")
        .int().default(4)

    private val outputToCsv: Boolean by option("-o", "--output", help = "Store results in CSV file")
        .flag(default = false)

    private val csvFilename: String? by option("-f", "--file", help = "CSV filename for storing results")

    private val repetitions: Int by option("-r", "--repeat", help = "Number of times to repeat the calculation")
        .int().default(1)

    private val enableLogging: Boolean by option("-l", "--log", help = "Enable detailed logging")
        .flag(default = false)

    private val concurrencyType: ConcurrencyType by option("-m", "--mode",
        help = "Concurrency mode: COORDINATOR, FAN or PURE")
        .enum<ConcurrencyType>().default(ConcurrencyType.COORDINATOR)

    private val mode: String
        get() = "kt_" + concurrencyType.name.lowercase()

    private val exportService = CsvExportService()

    private val matrixProduct: MatrixProduct
        get() = MatrixProductFactory.create(concurrencyType)

    override fun run() = runBlocking {
        withLog { info { "[size: $size, coroutines: $coroutines, output: $outputToCsv, file: $csvFilename, repetitions: $repetitions, mode: $concurrencyType]" } }

        val results = executeRuns()
        logStatistics(results)
        exportResults(results)
    }

    private fun exportResults(results: Collection<RunResult>) {
        if (outputToCsv) {
            val exportInfos = results.map { result ->  ExportInfo(
                size = size,
                coroutines = coroutines,
                mode = mode,
                executionTimeMillis = result.elapsedTimeMillis
            )}

            val workspaceExportInfo = WorkspaceExportInfo(exportService.createWorkspace(), exportInfos)
            val exportFile = exportService.saveExports(workspaceExportInfo, csvFilename)
            withLog { info { "results exported into $exportFile" } }
        }
    }

    private fun logStatistics(results: Collection<RunResult>) {
        val executionTimes = results.map { it.elapsedTimeMillis }
        val avgTime = executionTimes.average()
        val minTime = executionTimes.minOrNull() ?: 0
        val maxTime = executionTimes.maxOrNull() ?: 0

        withLog { info { "execution statistics: [average: $avgTime ms, min: $minTime, max: $maxTime]" } }
    }

    private suspend fun executeRuns(): List<RunResult> {
        val matA = MatrixUtils.createRandomMatrix(size)
        val matB = MatrixUtils.createRandomMatrix(size)

        val results = mutableListOf<RunResult>()
        repeat(repetitions) { iteration ->
            withLog { info { "running execution ${iteration + 1}/$repetitions..." } }
            val matC: Array<IntArray>
            val executionTime = measureTime {
                matC = matrixProduct.multiply(matA, matB, coroutines)
            }.inWholeMilliseconds

            results.add(RunResult(
                iteration = iteration,
                matA = matA,
                matB = matB,
                result = matC,
                elapsedTimeMillis = executionTime))
            withLog { info { "execution ${iteration + 1}: elapsed time: $executionTime ms" } }
        }

        withLog { info { "executions completed" } }
        return results
    }

    private fun withLog(block: KLogger.() -> Unit) {
        if (enableLogging) {
            log.block()
        }
    }
}