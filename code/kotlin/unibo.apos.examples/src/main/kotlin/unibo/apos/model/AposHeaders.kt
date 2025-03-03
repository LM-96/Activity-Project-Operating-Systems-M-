package unibo.apos.model

enum class AposHeaders(val code: String) {
    WORKSPACE_ID("workspaceId"),
    SIZE("size"),
    COROUTINES("coroutine"),
    MODE("mode"),
    TIME_MILLIS("timeMillis"),;

    companion object {
        val ALL: Array<String> = entries
            .map { it.code }
            .toTypedArray()
    }
}