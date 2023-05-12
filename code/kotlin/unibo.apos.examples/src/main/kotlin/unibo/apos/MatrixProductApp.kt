import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import unibo.apos.matrix.Matrix
import unibo.apos.matrix.algebra.product.*
import unibo.apos.matrix.createMatrix
import unibo.apos.minifsm.builders.miniWork
import kotlin.random.Random
import kotlin.system.measureNanoTime

data class ProgramConfiguration(
    val matrixDimension: Int,
    val concurrentUnits: Int,
    val chosenMatrixMultiplierNames: List<MatrixMultiplierName> = MatrixMultiplierName.values().toList()
)

data class MultiplierExecutionStats(
    val multiplierName: MatrixMultiplierName,
    val programmingLanguage: String,
    val matrixDimension: Int,
    val concurrentUnits: Int,
    val elapsedNanos: Long
)

fun main(args: Array<String>) {
    miniWork("main") {
        initialState = "POPULATE_MATRICES"
        val configuration = parseConfiguration(args)
        val multiplierExecutionStats = mutableListOf<MultiplierExecutionStats>()
        val matA: Matrix = createMatrix(configuration.matrixDimension, configuration.matrixDimension)
        val matB: Matrix = createMatrix(configuration.matrixDimension, configuration.matrixDimension)

        state("POPULATE_MATRICES") {
            action = {
                miniPrintln("populating matrices...")
                for (r in 0 until configuration.matrixDimension) {
                    for(c in 0 until configuration.matrixDimension) {
                        matA[r][c] = Random.nextDouble(20.0) - 10
                        matB[r][c] = Random.nextDouble(20.0) - 10
                    }
                }
                miniPrintln("matrices populated")
            }
            transition {
                destination = "EXECUTING_PRODUCTS"
            }
        }

        state("EXECUTING_PRODUCTS") {
            action = {
                configuration.chosenMatrixMultiplierNames
                    .map { Pair(it, MatrixMultiplierFactory.create{
                        matrixMultiplierName = it
                        concurrentUnits = configuration.concurrentUnits
                    }) }
                    .forEach {
                        miniPrintln("executing product with ${it.first}...")
                        val multiplier: MatrixMultiplier = it.second
                        var elapsedNanos = measureNanoTime {
                            multiplier.multiply(matA, matB)
                        }

                        val stats = MultiplierExecutionStats(it.first,
                            "kotlin", configuration.matrixDimension, configuration.concurrentUnits,
                            elapsedNanos)
                        multiplierExecutionStats.add(stats)
                        miniPrintln("executed [$stats]")
                    }
            }
        }
    }
}

fun parseConfiguration(args: Array<String>): ProgramConfiguration {
    val parser = ArgParser("MatrixProductApp")
    val matrixDimension: Int by parser.option(ArgType.Int, shortName = "d", description = "matrix dimension").required()
    val concurrentUnits: Int by parser.option(ArgType.Int, shortName = "t", description = "concurrent units").required()
    parser.parse(args)

    return ProgramConfiguration(matrixDimension, concurrentUnits)
}

