package unibo.apos.matrix.product

import unibo.apos.matrix.product.impl.CoordinatorChanneledMatrixProductImpl
import unibo.apos.matrix.product.impl.FanChanneledMatrixProductImpl

object MatrixProductFactory {

    fun create(concurrencyType: ConcurrencyType): MatrixProduct {
        return when (concurrencyType) {
            ConcurrencyType.COORDINATOR -> CoordinatorChanneledMatrixProductImpl()
            ConcurrencyType.FAN -> FanChanneledMatrixProductImpl()
        }
    }

}