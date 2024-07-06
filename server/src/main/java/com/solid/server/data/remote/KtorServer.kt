package com.solid.server.data.remote

import android.content.SharedPreferences
import com.solid.dto.ClientCommands
import com.solid.server.data.remote.plugins.configureSockets
import com.solid.server.utils.PORT_CONFIG
import io.ktor.server.cio.CIO
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.routing.routing
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.job
import kotlinx.serialization.json.Json
import kotlin.random.Random

class KtorServer(private val portConfPrefs : SharedPreferences) : ScanServer {

    private var server : ApplicationEngine? = null
    private var socket : WebSocketSession? = null

    private val clientCommandsChannel = Channel<ClientCommands>()
    override val clientCommands = clientCommandsChannel.receiveAsFlow()

    private val _isClientConnected = MutableStateFlow(false)
    override val isClientConnected = _isClientConnected.asStateFlow()

    override suspend fun startServer(){

        val port = portConfPrefs.getInt(PORT_CONFIG, 23456)

        server = embeddedServer(factory = CIO, port = port){

            configureSockets()

            routing {

                webSocket("/connect") {

                    if(_isClientConnected.value) {
                        this.close(
                            CloseReason(
                                CloseReason.Codes.CANNOT_ACCEPT,
                                "Server already in use"
                            )
                        )
                        return@webSocket
                    }

                    socket = this@webSocket

                    _isClientConnected.update { true }

                    socket?.coroutineContext?.job?.invokeOnCompletion {
                        _isClientConnected.update { false }
                    }

                    for (frame in incoming) {

                        frame as? Frame.Text ?: continue

                        val clientCommand = Json.decodeFromString<ClientCommands>(frame.readText())

                        clientCommandsChannel.send(clientCommand)

                    }
                }
            }
        }

        server?.start()

    }


    override suspend fun sendJsonResponseToClient(json : String){
        socket?.send(Frame.Text(json))
    }


    override suspend fun stopServer(){
        socket?.close()
        socket = null
        server?.stop()
        server = null
    }



}