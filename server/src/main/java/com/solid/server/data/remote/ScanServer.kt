package com.solid.server.data.remote

import com.solid.dto.ClientCommands
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ScanServer {

    val clientCommands : Flow<ClientCommands>
    val isClientConnected : StateFlow<Boolean>

    suspend fun startServer()

    suspend fun stopServer()

    suspend fun sendJsonResponseToClient(json : String)


}