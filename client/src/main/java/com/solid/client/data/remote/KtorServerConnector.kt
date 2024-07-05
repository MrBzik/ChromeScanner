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

            socket?.incoming?.consumeEach { frame ->

                if (frame !is Frame.Text) return@consumeEach

                val responseObj = Json.decodeFromString<ServerResponses>(frame.readText())

                when(responseObj){
                    is ServerResponses.MemoryStatus -> {
                        Logger.log("using: ${responseObj.memoryUsageKb}, available: ${responseObj.availableRamKb}")
                    }
                    is ServerResponses.NewScan -> {

//                        printTree(
//                            responseObj.scan.root
//                        )

                    }
                    is ServerResponses.ScanRecoveryResults -> {


                    }
                    is ServerResponses.ScansList -> {

                        Logger.log("GOT: ${responseObj.scansList.size}")

                    }
                }

            }
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

    override suspend fun recoverFileSystem(id: String) {

        if (socket?.isActive == false) return

        val command = ClientCommands.RecoverFileSystem(id)

        val json = Json.encodeToString(ClientCommands.serializer(), command)

        socket?.send(Frame.Text(json))

    }
}