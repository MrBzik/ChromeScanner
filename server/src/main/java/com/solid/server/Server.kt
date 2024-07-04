package com.solid.server

import com.solid.dto.ClientCommands
import com.solid.server.utils.Logger
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.serialization.json.Json
import java.time.Duration

fun Application.module(){

    install(ContentNegotiation){
        json()
    }

    install(WebSockets){
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }


    routing {
        get("/") {
            call.respond(HttpStatusCode.OK, "S U C C E S S")
        }
    }

    routing {

        webSocket("/connect") {

            send(Frame.Text("Hello client"))
            for (frame in incoming) {
                frame as? Frame.Text ?: continue
                Logger.log(frame.readText())
                val testClass = Json.decodeFromString<ClientCommands>(frame.readText())

                Logger.log("GOT HERE")
               when(testClass){
                   is ClientCommands.RecoverFileSystem -> Logger.log("Recovering: ${testClass.fileSystemID}")
                   is ClientCommands.StartScan -> Logger.log("START SCAN")
                   is ClientCommands.StopScan -> Logger.log("STOP SCAN")
               }


            }

        }
    }
}