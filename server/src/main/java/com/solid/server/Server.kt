package com.solid.server
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.testConnection(){

    routing {
        get("/") {
            call.respond(HttpStatusCode.OK, "S U C C E S S")
        }
    }
}