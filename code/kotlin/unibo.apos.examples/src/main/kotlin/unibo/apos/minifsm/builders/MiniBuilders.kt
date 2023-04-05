package unibo.apos.minifsm.builders

import unibo.apos.minifsm.*
import unibo.apos.minifsm.builders.exceptions.BuildException
import java.util.*

fun <I : Any, O> miniWork(fsmName: String, inputs: Iterable<I>, miniFsmBuilder: MiniFsmModel<I, O>.() -> Unit) {
    inputs.iterator().apply {
        MiniFsmBuilder<I, O>(fsmName)
            .apply(miniFsmBuilder)
            .build()
            .work { if(this.hasNext()) Optional.of(this.next()) else Optional.empty() }
    }
}

fun miniWork(fsmName: String, miniFsmBuilder: MiniFsmModel<Unit, Unit>.() -> Unit) {
    MiniFsmBuilder<Unit, Unit>(fsmName)
        .apply(miniFsmBuilder)
        .build()
        .apply {
            this.work { Optional.of(Unit) }
        }
}

@Throws(BuildException::class)
fun <I, O> miniFsm(name: String? = null, initialState: String? = null, fsmBuilder: MiniFsmModel<I, O>.() -> Unit): MiniFsm<I, O> {
    return MiniFsmBuilder<I, O>(name, initialState)
        .apply(fsmBuilder)
        .build()
}

@Throws(DuplicatedStateNameException::class, ReservedStateNameException::class)
fun validateStateNamesOrThrow(fsmName: String, states: Iterable<MiniState<*, *>>) {
    val stateNames: MutableList<String> = mutableListOf()
    states.forEach {
        if (stateNames.contains(it.name))
            throw DuplicatedStateNameException(fsmName, it)
        stateNames.add(it.name)
    }
}

@Throws()
fun validateStateTransitionsOrThrow(fsmName: String, states: Iterable<MiniState<*, *>>) {
    val stateNames = states.map { it.name }
    states.forEach { state ->
        val transitions: Set<MiniTransition<*>> = state.transitions
        transitions.forEach { transition ->
            if(transition.sourceStateName != state.name)
                throw IllegalTransitionOwnerException(fsmName, state.name, transition)
            if(!stateNames.contains(transition.destinationStateName)) {
                throw LostTargetTransitionException(fsmName, state.name, transition)
            }
        }
    }
}