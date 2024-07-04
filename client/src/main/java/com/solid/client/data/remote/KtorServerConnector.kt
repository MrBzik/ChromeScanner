package com.solid.client.data.remote

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

class KtorServerConnector(private val client: HttpClient) : ServerConnector {

    private var socket: WebSocketSession? = null



    override suspend fun establishConnection() {

//        socket = client.webSocketSession(method = HttpMethod.Get, host = "10.0.2.2", port = 12345)

//        client.webSocket(method = HttpMethod.Get, host = "10.0.2.2", port = 12345){
//
//        }

        if(socket?.isActive == true){

            socket?.send(Frame.Text("hello server"))

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