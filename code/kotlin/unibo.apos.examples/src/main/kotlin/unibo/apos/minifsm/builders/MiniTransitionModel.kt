package unibo.apos.minifsm.builders

interface MiniTransitionModel<I> {
    var destination: String?
    var canTransit: ((I) -> Boolean)?
    var elseDestination: String?
}