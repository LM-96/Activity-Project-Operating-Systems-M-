package unibo.apos.minifsm.builders
interface MiniStateModel<I, O> {
    var stateName: String?
    var action: ((I) -> O)?

    fun transition(transitionBuilder: MiniTransitionModel<I>.() -> Unit)
}