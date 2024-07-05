package com.solid.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface ServerResponses {

    @Serializable
    @SerialName("memory_status")
    data class MemoryStatus(val memoryUsageKb : Int, val availableRamKb : Int) : ServerResponses

    @Serializable
    @SerialName("scans_list")
    data class ScansList(val scansList : List<FileTreeScan>) : ServerResponses

    @Serializable
    @SerialName("new_scan")
    data class NewScan(val scan : FileTreeScan) : ServerResponses

    @Serializable
    @SerialName("recovery_results")
    data class ScanRecoveryResults(val isSuccess: Boolean, val message: String) : ServerResponses

}