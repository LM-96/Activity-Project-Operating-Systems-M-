package unibo.apos.minifsm

class NoViableTransition(val fsmName: String, val fsmState: String):
    Exception("$fsmName [state=$fsmState]: no transition found")

class NoSuchStateException(val fsmName: String, val fsmState: String):
        Exception("$fsmName: no state with name \'$fsmState\'")

open class IllegalStateNameException(val fsmName: String, val stateName: String, val reason: String):
        Exception("illegal name \'$stateName\' for state in fsm \'$fsmName\': $reason") {
            constructor(fsmName: String, state: MiniState<*, *>, reason: String): this(fsmName, state.name, reason)
        }

class DuplicatedStateNameException(fsmName: String, stateName: String) :
        IllegalStateNameException(fsmName, stateName, "a state with the name \'$stateName\' is already present: duplicated states are not allowed") {
            constructor(fsmName: String, state: MiniState<*, *>): this(fsmName, state.name)
        }

class ReservedStateNameException(fsmName: String, stateName: String) :
        IllegalStateNameException(fsmName, stateName, "unable to call a state \'${stateName}\': this name is reserved") {
            constructor(fsmName: String, state: MiniState<*, *>): this(fsmName, state.name)
        }

open class InconsistentTransitionException(val fsmName: String, val stateName: String, val transitionId: String, val reason: String):
        Exception("$fsmName [state=$stateName, transition=$transitionId]: inconsistent transition: $reason")

class IllegalTransitionOwnerException(fsmName: String, stateName: String, transitionId: String, val sourceState: String):
        InconsistentTransitionException(fsmName, stateName, transitionId,
            "transition has state \'$sourceState\' as source state but is owned by another state") {
            constructor(fsmName: String, stateName: String, transition: MiniTransition<*>) :
                    this(fsmName, stateName, transition.id, transition.sourceStateName)
        }

class LostTargetTransitionException(fsmName: String, stateName: String, transitionId: String, val targetState: String):
        InconsistentTransitionException(fsmName, stateName, transitionId,
            "transition has state \'$targetState\' as target but that state does not exist for fsm \'$fsmName\'") {
    constructor(fsmName: String, stateName: String, transition: MiniTransition<*>) :
            this(fsmName, stateName, transition.id, transition.destinationStateName)
        }

class NoActionPresentException(fsmName: String, stateName: String):
        RuntimeException("$fsmName [state=$stateName]: no action has been specified")