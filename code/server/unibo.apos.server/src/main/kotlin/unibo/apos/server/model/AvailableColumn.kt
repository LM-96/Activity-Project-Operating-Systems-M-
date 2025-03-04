package unibo.apos.server.model

enum class AvailableColumn(val code: String) {
    SIZE("size"),
    MAX_TIME("maxTime"),
    MIN_TIME("minTime"),
    AVG_TIME("avgTime"),
    WORKERS("workers")
}