package unibo.apos.server.model

enum class WorkspaceMode(val code: String) {
    KT_COORDINATOR("kt_coordinator"),
    KT_FAN("kt_fan"),
    KT_PURE("kt_pure"),
    GO_COORDINATOR("go_coordinator"),
    GO_FAN("go_fan"),
    GO_PURE("go_pure");

    companion object {
        fun fromCode(code: String): WorkspaceMode {
            return entries.find { it.code == code }!!
        }
    }
}