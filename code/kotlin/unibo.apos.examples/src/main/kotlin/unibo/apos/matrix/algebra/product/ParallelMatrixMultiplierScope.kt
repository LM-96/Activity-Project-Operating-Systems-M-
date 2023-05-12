package unibo.apos.matrix.algebra.product

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope

val PARALLEL_MATRIX_PRODUCT_EXECUTORS_SCOPE =
    CoroutineScope(CoroutineName("PARALLEL_MATRIX_PRODUCT_EXECUTORS_SCOPE"))

private var COUNTER: Int = 0
const val DEFAULT_CONCURRENT_UNITS = 10

fun openChildrenScopeForProductExecutor(clazz: Class<*>): CoroutineScope {
    return CoroutineScope(PARALLEL_MATRIX_PRODUCT_EXECUTORS_SCOPE.coroutineContext +
            CoroutineName("${clazz.simpleName}\$${COUNTER++}"))
}