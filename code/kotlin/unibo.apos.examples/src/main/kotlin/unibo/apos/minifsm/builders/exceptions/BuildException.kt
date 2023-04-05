package unibo.apos.minifsm.builders.exceptions

import java.util.*

class BuildException constructor(msg: String? = null, cause: Throwable? = null):
    Exception(msg, cause) {

    constructor(msg: String): this(msg, null)
    constructor(cause: Throwable): this(null, cause)

    init {
        if(Objects.isNull(msg) && Objects.isNull(cause))
            throw IllegalArgumentException("invalid constructor call: please specify a message or a cause for the exception")
    }

}