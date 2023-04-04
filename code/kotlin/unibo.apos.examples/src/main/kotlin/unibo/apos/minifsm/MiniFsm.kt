package unibo.apos.minifsm

class MiniFsm(
    val name: String,
    initial: String,
    private val states: Map<String, MiniState>
) {

    private var currentState: MiniState = MiniState.buildInitial(name, initial)

    fun transit() {

    }

}