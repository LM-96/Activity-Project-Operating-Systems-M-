package unibo.apos.minifsm

data class MiniTransition<I>(
    val id: String,
    val sourceStateName: String,
    val destinationStateName: String,
    val canTransit: (I) -> Boolean = {true}
)

fun <I> createTransitionForFsmWithGeneratedId(fsmName: String, sourceStateName: String,
                                          destinationStateName: String, canTransit: (I) -> Boolean = {true}): MiniTransition<I> {
    return MiniTransition("$fsmName\$${sourceStateName}_to_${destinationStateName}",
        sourceStateName, destinationStateName, canTransit)
}