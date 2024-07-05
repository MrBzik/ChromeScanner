package com.solid.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface ClientCommands {

    @Serializable
    @SerialName("start")
    data class StartScan(val intervalSec: Int) : ClientCommands

    @Serializable
    @SerialName("stop")
    data object StopScan : ClientCommands

    @Serializable
    @SerialName("recovery")
    data class RecoverFileSystem (val fileSystemID : Long) : ClientCommands

}