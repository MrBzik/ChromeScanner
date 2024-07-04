package com.solid.client

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.solid.client.presentation.MainVM
import com.solid.client.ui.theme.ChromiumBackupsTheme
import com.solid.dto.ClientCommands
import com.solid.server.utils.Logger
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.get
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel by viewModels<MainVM>()
        viewModel.initializeConnection()


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
