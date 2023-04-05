package unibo.apos.minifsm

import java.util.Optional

class MiniFsm(
    val name: String,
    initial: String,
    private val states: Map<String, MiniState>
) {

    private var currentState: MiniState = MiniState.buildInitial(name, initial)

    @Throws(NoTransitionException::class, NoSuchStateException::class)
    fun transit() {
        val performableTransitions: List<MiniTransition> = currentState.getEnabledTransition()
        if(performableTransitions.isEmpty())
            throw NoTransitionException(this.name, currentState.name)

        performableTransitions.first().destinationStateName.apply {
            currentState = states.getOrElse(this) {
                throw NoSuchStateException(name, this)
            }
        }
        currentState.action()
    }

    fun work() {
        var canWork: Boolean = true
        while(canWork) {
            try {
                transit()
            } catch (nte: NoTransitionException) {
                canWork = false
            }
        }
    }

}