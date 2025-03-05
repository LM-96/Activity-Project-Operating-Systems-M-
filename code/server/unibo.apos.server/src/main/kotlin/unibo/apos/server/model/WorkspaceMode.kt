package unibo.apos.server.model

enum class WorkspaceMode(val code: String) {
    KT_COORDINATOR("kt_coordinator"),
    KT_FAN("kt_fan"),
    KT_PURE("kt_pure"),
    KT_NTV_COORDINATOR("kt_ntv_coordinator"),
    KT_NTV_FAN("kt_ntv_fan"),
    KT_NTV_PURE("kt_ntv_pure"),
    KT_GRAAL_COORDINATOR("kt_graal_coordinator"),
    KT_GRAAL_FAN("kt_graal_fan"),
    KT_GRAAL_PURE("kt_graal_pure"),
    GO_COORDINATOR("go_coordinator"),
    GO_FAN("go_fan"),
    GO_PURE("go_pure");

    companion object {
        fun fromCode(code: String): WorkspaceMode {
            return entries.find { it.code == code }!!
        }
    }
}