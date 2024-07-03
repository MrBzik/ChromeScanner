package com.solid.server

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.module(){

    install(ContentNegotiation){
        json()
    }

    routing {
        get("/api/test") {
            call.respond(HttpStatusCode.OK, "S U C C E S S")
        }
    }

}