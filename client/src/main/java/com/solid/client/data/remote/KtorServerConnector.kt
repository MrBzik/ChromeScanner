package com.solid.client.data.remote

import com.solid.dto.ClientCommands
import com.solid.dto.ServerResponses
import com.solid.client.utils.Logger
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.Json

class KtorServerConnector(private val client: HttpClient) : ServerConnector {

    private var socket: WebSocketSession? = null

    private val serverResponsesChannel = Channel<ServerResponses>()
    override val serverResponses: Flow<ServerResponses> = serverResponsesChannel.receiveAsFlow()

    private val _isConnected = MutableStateFlow(false)
    override val isConnected = _isConnected.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    override val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    override suspend fun establishConnection(port: Int, host: String) {

        try {

            socket = client.webSocketSession(method = HttpMethod.Get, host = host, port = port, path = "/connect")

            _isConnected.value = socket?.isActive ?: false

            while (true){

                val frame = socket?.incoming?.receive()

                if(frame is Frame.Text){

                    val responseObj = Json.decodeFromString<ServerResponses>(frame.readText())

                    serverResponsesChannel.send(responseObj)
                }
            }

        } catch (e: Exception){
            Logger.log(e.stackTraceToString())
            _isConnected.value = false
            _isScanning.value = false
        }
    }

    override suspend fun startScanning(intervalsSec: Int) {

        if (socket?.isActive == false) return

        val command = ClientCommands.StartScan(intervalsSec)

        val json = Json.encodeToString(ClientCommands.serializer(), command)

        socket?.send(Frame.Text(json))

        _isScanning.value = true

    }

    override suspend fun stopScanning() {

        if (socket?.isActive == false) return

        val command = ClientCommands.StopScan

        val json = Json.encodeToString(ClientCommands.serializer(), command)

        socket?.send(Frame.Text(json))

        _isScanning.value = false

    }

    override suspend fun recoverFileSystem(id: Long) : Boolean {

        if (socket?.isActive == false) return false

        val command = ClientCommands.RecoverFileSystem(id)

        val json = Json.encodeToString(ClientCommands.serializer(), command)

        socket?.send(Frame.Text(json))

        return true

    }
}