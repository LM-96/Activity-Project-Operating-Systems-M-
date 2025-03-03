package unibo.apos

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.inject
import org.koin.ksp.generated.module
import unibo.apos.matrix.utils.MatrixUtils
import kotlin.system.measureTimeMillis
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import unibo.apos.matrix.product.ConcurrencyType
import unibo.apos.matrix.product.MatrixProduct
import unibo.apos.matrix.product.MatrixProductFactory
import unibo.apos.service.ExportService
import unibo.apos.model.ExportInfo
import unibo.apos.model.RunResult
import unibo.apos.model.WorkspaceExportInfo
import java.util.concurrent.Callable
import kotlin.system.exitProcess

@Command(
    name = "apos",
    mixinStandardHelpOptions = true,
    version = ["1.0"],
    description = ["Matrix multiplication analyzer using coroutines"]
)
class KAposMatrixApp : Callable<Int> {
    private val log = KotlinLogging.logger {}

    @Option(names = ["-s", "--size"], description = ["Size of the matrices (NxN)"], defaultValue = "3")
    private var size: Int = 3

    @Option(names = ["-c", "--coroutines"], description = ["Number of coroutines to use"], defaultValue = "4")
    private var coroutines: Int = 4

    @Option(names = ["-o", "--output"], description = ["Store results in CSV file"], defaultValue = "false")
    private var outputToCsv: Boolean = false

    @Option(names = ["-f", "--file"], description = ["CSV filename for storing results"], required = false)
    private var csvFilename: String? = null

    @Option(names = ["-r", "--repeat"], description = ["Number of times to repeat the calculation"], defaultValue = "1")
    private var repetitions: Int = 1

    @Option(names = ["-l", "--log"], description = ["Enable detailed logging"], defaultValue = "false")
    private var enableLogging: Boolean = false

    @Option(names = ["-m", "--mode"], description = ["Concurrency mode: COORDINATOR, FAN or PURE"], defaultValue = "COORDINATOR")
    private var concurrencyType: ConcurrencyType = ConcurrencyType.COORDINATOR

    private val mode: String
        get() = "kt_" + concurrencyType.name.lowercase()

    private val exportService: ExportService by inject(ExportService::class.java)

    private val matrixProduct: MatrixProduct
        get() = MatrixProductFactory.create(concurrencyType)

    override fun call(): Int = runBlocking {
        withLog { info {  "[size: $size, coroutines: $coroutines, output: $outputToCsv, file: $csvFilename, repetitions: $repetitions, mode: $concurrencyType]" } }
        setupInjection();

        val results = run();
        logStatistics(results)
        exportResults(results)

        return@runBlocking 0
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

        withLog { info { "execution statistics: [average: ${"%.2f".format(avgTime)} ms, min: $minTime, max: $maxTime]" } }
    }

    private suspend fun run(): List<RunResult> {
        val matA = MatrixUtils.createRandomMatrix(size)
        val matB = MatrixUtils.createRandomMatrix(size)

        val results = mutableListOf<RunResult>()
        repeat(repetitions) { iteration ->
            withLog { info { "running execution ${iteration + 1}/$repetitions..." } }
            val matC: Array<IntArray>
            val executionTime = measureTimeMillis {
                matC = matrixProduct.multiply(matA, matB, coroutines)
            }

            results.add(RunResult(
                iteration = iteration,
                matA = matA,
                matB = matB,
                result = matC,
                elapsedTimeMillis = executionTime))
            withLog { info { "execution ${iteration + 1}: elapsed time: $executionTime ms" } }
        }

        withLog { info { "executions completed" } }
        return results;
    }

    private fun setupInjection() {
        startKoin() {
            if (enableLogging) {
                printLogger()
            }
            modules(AposKoinModule().module)
        }
    }

    private fun withLog(block: KLogger.() -> Unit) {
        if (enableLogging) {
            log.block()
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val cmd = CommandLine(KAposMatrixApp())
            cmd.usageHelpWidth = 120
            val exitCode = cmd.execute(*args)
            exitProcess(exitCode)
        }
    }
}