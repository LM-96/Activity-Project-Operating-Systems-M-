package unibo.apos.minifsm

data class MiniTransition(
    val id: String,
    val sourceStateName: String,
    val destinationStateName: String,
    val canTransit: () -> Boolean = {true}
)

fun createTransitionForFsmWithGeneratedId(fsmName: String, sourceStateName: String,
                                          destinationStateName: String, canTransit: () -> Boolean = {true}): MiniTransition {
    return MiniTransition("$fsmName\$${sourceStateName}_to_${destinationStateName}",
        sourceStateName, destinationStateName, canTransit)
}