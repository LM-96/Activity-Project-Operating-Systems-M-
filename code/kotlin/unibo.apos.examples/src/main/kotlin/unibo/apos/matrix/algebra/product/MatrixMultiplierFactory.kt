package unibo.apos.matrix.algebra.product

object MatrixMultiplierFactory {

    class CreationOptions {
        var matrixMultiplierName: MatrixMultiplierName? = null
        var concurrentUnits: Int? = null
    }

    fun create(name: MatrixMultiplierName): MatrixMultiplier {
        return create { matrixMultiplierName = name }
    }

    fun create(options: CreationOptions): MatrixMultiplier {
        if(options.matrixMultiplierName == null) {
            throw IllegalStateException("configuration must specify the matrix multiplier name")
        }

        return when(options.matrixMultiplierName) {
            MatrixMultiplierName.PARALLEL_DEDICATED_CHANNEL ->
                ParallelDedicatedChannelGuidedMatrixMultiplier(options.concurrentUnits ?: DEFAULT_CONCURRENT_UNITS)
            MatrixMultiplierName.PARALLEL_SINGLE_CHANNEL ->
                ParallelSingleChannelGuidedMatrixMultiplier(options.concurrentUnits ?: DEFAULT_CONCURRENT_UNITS)
            MatrixMultiplierName.SEQUENTIAL_IJK -> SeqIJKMatrixMultiplier()
            MatrixMultiplierName.SEQUENTIAL_IKJ -> SeqIKJMatrixMultiplier()

            else -> throw IllegalArgumentException("invalid name ${options.matrixMultiplierName}")
        }
    }

    fun create(initOptions: CreationOptions.() -> Unit): MatrixMultiplier {
        return create(CreationOptions().apply(initOptions))
    }

    fun createAll(): Array<MatrixMultiplier> {
        return MatrixMultiplierName.values()
            .map { create(it) }
            .toTypedArray()
    }

}