package unibo.apos.minifsm

import java.util.Optional

data class MiniState<I, O>(
    val name: String,
    val fsmName: String,
    val transitions: Set<MiniTransition<I>>,
    val action: (I) -> O
) {

    fun getEnabledTransitionsForInput(input: I): List<MiniTransition<I>> =
        transitions.filter { it.canTransit(input) }

    fun getFirstEnabledTransitionForInput(input: I): Optional<MiniTransition<I>> =
        Optional.ofNullable(transitions.find { it.canTransit(input) })
}