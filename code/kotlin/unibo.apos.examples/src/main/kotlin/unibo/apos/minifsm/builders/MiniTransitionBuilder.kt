package unibo.apos.minifsm.builders

import unibo.apos.minifsm.MiniTransition
import unibo.apos.minifsm.builders.exceptions.BuildException
import java.util.*

class MiniTransitionBuilder<I>(
    private var transitionId: String? = null,
    private var source: String? = null,
    override var destination: String? = null,
    override var canTransit: ((I) -> Boolean)? = null,
    override var elseDestination: String? = null
): MiniTransitionModel<I> {

    @Throws(BuildException::class)
    fun build(): Set<MiniTransition<I>> {
        if(Objects.isNull(transitionId))
            throw BuildException("transitionId can not be null")
        if(Objects.isNull(source))
            throw BuildException("source can not be null")
        if(Objects.isNull(destination))
            throw BuildException("destination can not be null")
        if(Objects.isNull(canTransit))
            canTransit = {true}

        return if(Objects.isNull(elseDestination)) {
            setOf(MiniTransition(transitionId!!, source!!, destination!!, canTransit!!))
        } else {
            setOf(
                MiniTransition(transitionId!!, source!!, destination!!, canTransit!!),
                MiniTransition("$transitionId.else", source!!, elseDestination!!) { !canTransit!!(it) }
            )
        }
    }

}