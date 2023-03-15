package unibo.apos

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.required
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import unibo.apos.matrix.Matrix
import unibo.apos.matrix.algebra.product.SeqIJKMatrixProductExecutor
import unibo.apos.matrix.algebra.product.SeqIKJMatrixProductExecutor
import unibo.apos.matrix.createMatrix
import java.nio.file.StandardOpenOption
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import kotlin.io.path.Path
import kotlin.io.path.bufferedWriter
import kotlin.random.Random

val DEFAULT_RANDOM_DOUBLE_BOUND: Double = 10.0
val PRODUCT_EXECUTORS = arrayOf(
    SeqIJKMatrixProductExecutor(),
    SeqIKJMatrixProductExecutor()
)
fun generateRandomMatrix(row: Int, columns: Int, randomBound: Double = DEFAULT_RANDOM_DOUBLE_BOUND): Matrix {
    return createMatrix(row, columns) { _, _ -> Random.Default.nextDouble(randomBound) }
}

data class Arguments(
    val dim: Int,
    val threads: Int,
    val randomBound: Double,
    val csvPath: String?
)

data class ResultCsvEntry(
    val language: String,
    val method: String,
    val threads: Int,
    val matrixDimension: Int,
    val date: LocalDateTime,
    val elapsedMillis: Long
)

fun parseArgs(args: Array<String>): Arguments {
    val parser = ArgParser("MatrixProductApp")
    val dim by parser
        .option(ArgType.Int, fullName = "dim", shortName = "d", description = "the dimension of the matrix")
        .required()
    val randomBound by parser
        .option(ArgType.Double, fullName = "random-bound", shortName = "rb",
            description = "the bound for the generation of the random elements of the matrix")
        .default(DEFAULT_RANDOM_DOUBLE_BOUND)
    val csvPath by parser
        .option(ArgType.String, fullName = "csv-path", shortName = "csv", description = "the path of the csv file to store the results")
    val threads by parser
        .option(ArgType.Int, fullName = "thread-number", shortName = "t", description = "the number of the threads/coroutines to be used")
        .default(1)

    parser.parse(args)
    return Arguments(dim, threads, randomBound, csvPath)
}

fun saveResults(csvPath: String, results: Collection<ResultCsvEntry>) {
    val writer = Path(csvPath)
        .bufferedWriter(options = arrayOf(StandardOpenOption.CREATE, StandardOpenOption.APPEND))
    val csvFormat = CSVFormat.Builder
        .create(CSVFormat.DEFAULT)
        .setHeader("language", "method", "threads", "matrix-dimension", "date", "elapsed-millis")
        .build()
    val csvPrinter = CSVPrinter(writer, csvFormat)
    csvPrinter.use { printer ->
        results.forEach { entry ->
            printer.printRecord(entry.language, entry.method, entry.threads, entry.matrixDimension, entry.date, entry.elapsedMillis)
        }
        printer.flush()
    }
}

fun main(args: Array<String>) {
    println("Welcome to the matrix product test in Kotlin")

    val parsedArgs = parseArgs(args)
    val matA: Matrix = generateRandomMatrix(parsedArgs.dim, parsedArgs.dim, parsedArgs.randomBound)
    val matB: Matrix = generateRandomMatrix(parsedArgs.dim, parsedArgs.dim, parsedArgs.randomBound)

    println("Starting test with options: [$parsedArgs]")
    val results = PRODUCT_EXECUTORS
        .map {
            val date = LocalDateTime.now()
            val startTime = System.nanoTime()
            it.multiply(matA, matB)
            val endTime = System.nanoTime()

            var threads: Int = if(it.javaClass.simpleName.startsWith("Seq")) 1 else parsedArgs.threads
            ResultCsvEntry("kotlin", it.javaClass.simpleName, threads, parsedArgs.dim,
                date, TimeUnit.NANOSECONDS.toMillis(endTime - startTime))
        }

    results.forEach(System.out::println)

}