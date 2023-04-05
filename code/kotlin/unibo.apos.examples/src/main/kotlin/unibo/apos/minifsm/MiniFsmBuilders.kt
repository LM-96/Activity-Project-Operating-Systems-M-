package unibo.apos.minifsm

import java.util.Objects

class BuildException constructor(msg: String? = null, cause: Throwable? = null):
    Exception(msg, cause) {

        constructor(msg: String): this(msg, null)
        constructor(cause: Throwable): this(null, cause)

    init {
        if(Objects.isNull(msg) && Objects.isNull(cause))
            throw IllegalArgumentException("invalid constructor call: please specify a message or a cause for the exception")
    }

}

interface MiniTransitionModel {
    var destination: String?
    var canTransit: (() -> Boolean)?
    var elseDestination: String?
}

interface MiniStateModel {
    var stateName: String?
    var action: (() -> Unit)?

    fun transition(transitionBuilder: MiniTransitionModel.() -> Unit)
}

interface MiniFsmModel {
    var fsmName: String?
    var initialState: String?

    fun state(stateBuilder: MiniStateBuilder.() -> Unit)
    fun state(name: String, stateBuilder: MiniStateBuilder.() -> Unit)
}

class MiniTransitionBuilder(
    private var transitionId: String? = null,
    private var source: String? = null,
    override var destination: String? = null,
    override var canTransit: (() -> Boolean)? = null,
    override var elseDestination: String? = null
): MiniTransitionModel {

    @Throws(BuildException::class)
    fun build(): Set<MiniTransition> {
        if(Objects.isNull(transitionId))
            throw BuildException("transitionId can not be null")
        if(Objects.isNull(source))
            throw BuildException("source can not be null")
        if(Objects.isNull(destination))
            throw BuildException("destination can not be null")
        if(Objects.isNull(canTransit))
            canTransit = {true}
        
        if(Objects.isNull(elseDestination)) {
            return setOf(MiniTransition(transitionId!!, source!!, destination!!, canTransit!!))
        } else {
            return setOf(
                MiniTransition(transitionId!!, source!!, destination!!, canTransit!!),
                MiniTransition("$transitionId.else", source!!, elseDestination!!) { !canTransit!!() }
            )
        }
    }

}

class MiniStateBuilder(
    private var fsmName: String? = null,
    override var stateName: String? = null,
    override var action: (() -> Unit)? = null
): MiniStateModel {

    private val transitions: MutableSet<MiniTransition> = mutableSetOf()
    private var counter: Int = 0

    private fun newTransitionId(): String {
        return "$fsmName\\$stateName\$${counter++}"
    }

    override fun transition(transitionBuilder: MiniTransitionModel.() -> Unit) {
        val transition = MiniTransitionBuilder(newTransitionId(), stateName)
            .apply(transitionBuilder)
            .build()
        transitions.addAll(transition)
    }

    @Throws(BuildException::class)
    fun build(): MiniState {
        if(Objects.isNull(fsmName))
            throw BuildException("fsmName can not be null")
        if(Objects.isNull(stateName))
            throw BuildException("name can not be null")
        if(Objects.isNull(action))
            action = {}

        return MiniState(stateName!!, transitions, action!!)
    }
}

class MiniFsmBuilder(
    override var fsmName: String? = null,
    override var initialState: String? = null
): MiniFsmModel {

    private val states: MutableSet<MiniState> = mutableSetOf()

    override fun state(stateBuilder: MiniStateBuilder.() -> Unit) {
        val state = MiniStateBuilder(fsmName)
            .apply(stateBuilder)
            .build()
        states.add(state)
    }

    override fun state(name: String, stateBuilder: MiniStateBuilder.() -> Unit) {
        val state = MiniStateBuilder(fsmName, name)
            .apply(stateBuilder)
            .build()
        states.add(state)
    }

    fun build(): MiniFsm {
        if(Objects.isNull(fsmName))
            throw BuildException("name can not be null")
        if(Objects.isNull(initialState))
            throw BuildException("initialState can not be null")

        return MiniFsm(fsmName!!, initialState!!, states.associateBy { it.name })
            .apply { validateStateNamesOrThrow(this@MiniFsmBuilder.fsmName!!, this@MiniFsmBuilder.states) }
            .apply { validateStateTransitionsOrThrow(this@MiniFsmBuilder.fsmName!!, this@MiniFsmBuilder.states) }
    }

}

@Throws(BuildException::class)
fun miniFsm(name: String? = null, initialState: String? = null, fsmBuilder: MiniFsmModel.() -> Unit): MiniFsm {
    return MiniFsmBuilder(name, initialState)
        .apply(fsmBuilder)
        .build()
}

@Throws(DuplicatedStateNameException::class, ReservedStateNameException::class)
fun validateStateNamesOrThrow(fsmName: String, states: Iterable<MiniState>) {
    val stateNames: MutableList<String> = mutableListOf()
    states.forEach {
        if(it.name == MiniState.INIT_STATE_NAME )
            throw ReservedStateNameException(fsmName, it)
        if (stateNames.contains(it.name))
            throw DuplicatedStateNameException(fsmName, it)
        stateNames.add(it.name)
    }
}

@Throws()
fun validateStateTransitionsOrThrow(fsmName: String, states: Iterable<MiniState>) {
    val stateNames = states.map { it.name }
    states.forEach { state ->
        val transitions: Set<MiniTransition> = state.transitions
        transitions.forEach { transition ->
            if(transition.sourceStateName != state.name)
                throw IllegalTransitionOwnerException(fsmName, state.name, transition)
            if(!stateNames.contains(transition.destinationStateName)) {
                throw LostTargetTransitionException(fsmName, state.name, transition)
            }
        }
    }
}