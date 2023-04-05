package unibo.apos.minifsm.builders

interface MiniFsmModel<I, O> {
    var fsmName: String?
    var initialState: String?

    fun state(stateBuilder: MiniStateModel<I, O>.() -> Unit)
    fun state(name: String, stateBuilder: MiniStateModel<I, O>.() -> Unit)

    fun MiniStateModel<I, O>.miniFormat(line: String): String {
        return "$fsmName [state=$stateName]: $line"
    }

    fun miniFormat(line: String): String {
        return "$fsmName: $line"
    }

    fun MiniStateModel<I, O>.miniPrintln(line: String) {
        println(miniFormat(line))
    }

    fun MiniStateModel<I, O>.miniPrint(line: String) {
        print(miniFormat(line))
    }

    fun miniPrint(line: String) {
        print(miniFormat(line))
    }

    fun miniPrintln(line: String) {
        println(miniFormat(line))
    }
}