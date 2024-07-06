package com.solid.client.data.remote

import com.solid.client.fileutils.printTree
import com.solid.dto.ClientCommands
import com.solid.dto.ServerResponses
import com.solid.server.utils.Logger
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.readText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class KtorServerConnector(private val client: HttpClient) : ServerConnector {

    private var socket: WebSocketSession? = null

    private val serverResponsesChannel = Channel<ServerResponses>()
    override val serverResponses: Flow<ServerResponses> = serverResponsesChannel.receiveAsFlow()

    override suspend fun establishConnection() {

        try {
            socket = client.webSocketSession(method = HttpMethod.Get, host = "10.0.2.2", port = 12345, path = "/connect")
            while (true){

                val frame = socket?.incoming?.receive()

                if(frame is Frame.Text){

                    val responseObj = Json.decodeFromString<ServerResponses>(frame.readText())
                    serverResponsesChannel.send(responseObj)
                }
            }

        } catch (e: Exception){
            Logger.log(e.stackTraceToString())
        }
    }

    override suspend fun startScanning(intervalsSec: Int) {

        if (socket?.isActive == false) return

        val command = ClientCommands.StartScan(intervalsSec)

        val json = Json.encodeToString(ClientCommands.serializer(), command)

        socket?.send(Frame.Text(json))


//        Logger.log("IS SOCKET ACTIVE: ${socket?.isActive}")

    }

    override suspend fun stopScanning() {

    }

    override suspend fun recoverFileSystem(id: Long) {

        if (socket?.isActive == false) return

        val command = ClientCommands.RecoverFileSystem(id)

        val json = Json.encodeToString(ClientCommands.serializer(), command)

        socket?.send(Frame.Text(json))

    }
}