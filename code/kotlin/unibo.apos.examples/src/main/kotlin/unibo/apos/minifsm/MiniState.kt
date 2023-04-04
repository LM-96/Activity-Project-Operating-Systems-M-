package unibo.apos.minifsm

data class MiniState(
    val name: String,
    val transitions: Set<MiniTransition>,
    val action: () -> Unit = {}
) {

    companion object {
        const val INIT_STATE_NAME: String = "init"

        fun buildInitial(fsmName: String, initialStateName: String): MiniState {
            return MiniState(INIT_STATE_NAME,
                setOf(createTransitionForFsmWithGeneratedId(fsmName, INIT_STATE_NAME, initialStateName)))
        }
    }

    fun getEnabledTransition(): List<MiniTransition> =
        transitions.filter { it.canTransit() }
}