package com.solid.client.data.remote

import com.solid.dto.ClientCommands
import com.solid.server.utils.Logger
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.readText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class KtorServerConnector(private val client: HttpClient) : ServerConnector {

    private var socket: WebSocketSession? = null



    override suspend fun establishConnection() {

        socket = client.webSocketSession(method = HttpMethod.Get, host = "10.0.2.2", port = 12345, path = "/connect")

        if(socket?.isActive == true){

            val command = Json.encodeToString(ClientCommands.serializer() , ClientCommands.RecoverFileSystem("123"))

            Logger.log(command)

            socket?.send(Frame.Text(command))

            socket?.incoming?.consumeEach {

                val othersMessage = it as? Frame.Text

                Logger.log(othersMessage?.readText() ?: "NO MESSAGE")

            }
        }
    }

    override suspend fun startScanning() {

    }

    override suspend fun stopScanning() {

    }

    override suspend fun recoverFileSystem(id: String) {

    }
}