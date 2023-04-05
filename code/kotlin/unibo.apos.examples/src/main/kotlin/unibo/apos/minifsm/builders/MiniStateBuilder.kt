package unibo.apos.minifsm.builders

import unibo.apos.minifsm.MiniState
import unibo.apos.minifsm.MiniTransition
import unibo.apos.minifsm.NoActionPresentException
import unibo.apos.minifsm.builders.exceptions.BuildException
import java.util.*

class MiniStateBuilder<I, O>(
    private var fsmName: String? = null,
    override var stateName: String? = null,
    override var action: ((I) -> O)? = null
): MiniStateModel<I, O> {

    private val transitions: MutableSet<MiniTransition<I>> = mutableSetOf()
    private var counter: Int = 0

    private fun newTransitionId(): String {
        return "$fsmName\\$stateName\$${counter++}"
    }

    override fun transition(transitionBuilder: MiniTransitionModel<I>.() -> Unit) {
        val transition = MiniTransitionBuilder<I>(newTransitionId(), stateName)
            .apply(transitionBuilder)
            .build()
        transitions.addAll(transition)
    }

    @Throws(BuildException::class)
    fun build(): MiniState<I, O> {
        if(Objects.isNull(fsmName))
            throw BuildException("fsmName can not be null")
        if(Objects.isNull(stateName))
            throw BuildException("name can not be null")
        if(Objects.isNull(action))
            throw BuildException("action can not be null")

        return MiniState(stateName!!, fsmName!!, transitions, action!!)
    }
}