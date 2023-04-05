package unibo.apos.minifsm.builders

import unibo.apos.minifsm.MiniFsm
import unibo.apos.minifsm.MiniState
import unibo.apos.minifsm.builders.exceptions.BuildException
import java.util.*

class MiniFsmBuilder<I, O>(
    override var fsmName: String? = null,
    override var initialState: String? = null
): MiniFsmModel<I, O> {

    private val states: MutableSet<MiniState<I, O>> = mutableSetOf()

    override fun state(stateBuilder: MiniStateModel<I, O>.() -> Unit) {
        val state = MiniStateBuilder<I, O>(fsmName)
            .apply(stateBuilder)
            .build()
        states.add(state)
    }

    override fun state(name: String, stateBuilder: MiniStateModel<I, O>.() -> Unit) {
        val state = MiniStateBuilder<I, O>(fsmName, name)
            .apply(stateBuilder)
            .build()
        states.add(state)
    }

    fun build(): MiniFsm<I, O> {
        if(Objects.isNull(fsmName))
            throw BuildException("name can not be null")
        if(Objects.isNull(initialState))
            throw BuildException("initialState can not be null")

        return MiniFsm(fsmName!!, initialState!!, states.associateBy { it.name })
            .apply { validateStateNamesOrThrow(this@MiniFsmBuilder.fsmName!!, this@MiniFsmBuilder.states) }
            .apply { validateStateTransitionsOrThrow(this@MiniFsmBuilder.fsmName!!, this@MiniFsmBuilder.states) }
    }

}