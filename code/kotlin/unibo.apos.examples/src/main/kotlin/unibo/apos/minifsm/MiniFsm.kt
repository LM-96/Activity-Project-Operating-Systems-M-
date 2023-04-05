package unibo.apos.minifsm

import java.util.Optional

class MiniFsm<I, O>(
    val name: String,
    initial: String,
    private val states: Map<String, MiniState<I, O>>
) {

    var currentState: MiniState<I, O> = states.getOrElse(initial) {
        throw NoSuchStateException(name, initial)
    }
        private set

    @Throws(NoViableTransition::class, NoSuchStateException::class)
    fun process(input: I): Result<O> {
        return Result.runCatching {
            currentState.action(input)
        }.apply {
            currentState.getFirstEnabledTransitionForInput(input)
                .orElseThrow { NoViableTransition(this@MiniFsm.name, currentState.name) }
                .destinationStateName
                .apply {
                    currentState = states.getOrElse(this) {
                        throw NoSuchStateException(name, this)
                    }
                }
        }
    }

    fun work(inputProducer: () -> Optional<I>): Array<Result<O>> {
        var canWork: Boolean = true
        val results = mutableListOf<Result<O>>()
        while(canWork) {
            try {
                inputProducer().ifPresentOrElse({ //If present
                    results.add(process(it))
                }, { //If not present
                    canWork = false
                })
            } catch (nte: NoViableTransition) {
                canWork = false
                results.add(Result.failure(nte))
            }
        }

        return results.toTypedArray()
    }

}