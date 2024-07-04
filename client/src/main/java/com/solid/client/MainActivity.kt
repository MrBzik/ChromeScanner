package com.solid.client

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.solid.client.ui.theme.ChromiumBackupsTheme
import com.solid.server.utils.Logger
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.get
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var client: HttpClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        runBlocking {


// adb forward tcp:12345 tcp:23456
            client.webSocket(method = HttpMethod.Get, host = "10.0.2.2", port = 12345, path = "/connect"){
                while(true) {
                    val othersMessage = incoming.receive() as? Frame.Text
                    Logger.log(othersMessage?.readText() ?: "NO MESSAGE")
                    send(Frame.Text("hello server"))
                }
            }

        }





        enableEdgeToEdge()
        setContent {

            ChromiumBackupsTheme {
                Box (modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                    Text(text = "Hello Client!")
                }
            }
        }
    }
}
