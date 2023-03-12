package unibo.apos.matrix.validation.exceptions

import java.util.*

/**
 * An exception that could be thrown in the cases in which a row is invalid
 *
 * @property row the invalid row
 * @property reason the reason of the invalidity
 */
open class InvalidRowException :
    MatrixValidationException {

    val row: Array<*>
    val reason: Optional<String>

    constructor(row: Array<*>) : super("the row $row is invalid") {
        this.row = row
        this.reason = Optional.empty()
    }

    constructor(row: Array<*>, reason: String) : super("the row ${row.contentToString()} is invalid: $reason") {
        this.row = row
        this.reason = Optional.empty()
    }

    /**
     * Executes the given block if the reason of the invalidity is present
     *
     * @param block the block to execute
     * @receiver the reason of the invalidity
     * @return the exception itself
     */
    fun withReason(block: (String) -> Unit): InvalidRowException {
        if (reason.isPresent) {
            block(reason.get())
        }
        return this
    }

    /**
     * Executes the given block if the reason of the invalidity is not present
     *
     * @param block the block to execute
     * @receiver
     * @return
     */
    fun withoutReason(block: () -> Unit): InvalidRowException {
        if (reason.isEmpty) {
            block()
        }
        return this
    }

}