package com.solid.server

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.core.app.ActivityCompat
import com.solid.server.presentation.MainVm
import com.solid.server.service.ScanningService
import com.solid.server.service.ServiceActions
import com.solid.server.ui.SettingsDialog
import com.solid.server.ui.theme.ChromiumBackupsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0
            )
        }

        setContent {
            ChromiumBackupsTheme {

                val isToShowSettingsDialog = remember {
                    mutableStateOf(false)
                }

                val viewModel by viewModels<MainVm>()

                val serverLogs = ScanningService.serverLogs.collectAsState()
                
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly, horizontalAlignment = Alignment.CenterHorizontally) {
                    
                    Button(onClick = {
                        Intent(applicationContext, ScanningService::class.java).also {
                            it.action = ServiceActions.START.toString()
                            startService(it)
                        }
                    }) {
                        Text(text = "START SERVER")
                    }

                    Button(onClick = {
                        Intent(applicationContext, ScanningService::class.java).also {
                            it.action = ServiceActions.STOP.toString()
                            startService(it)
                        }
                    }) {
                        Text(text = "STOP SERVER")
                    }

                    Button(onClick = {
                        isToShowSettingsDialog.value = true

                    }) {
                        Text(text = "CONFIGURE SERVER")
                    }


                    DisplayServerLogs {
                        serverLogs.value
                    }
                }


                if(isToShowSettingsDialog.value){
                    SettingsDialog(
                        onDismissRequest = { isToShowSettingsDialog.value = false },
                        onConfirmation = { port ->
                            val success = viewModel.updatePort(port)
                            if(success){
                                isToShowSettingsDialog.value = false
                            } else {
                                Toast.makeText(this@MainActivity, "Неверные данные", Toast.LENGTH_SHORT).show()
                            }
                            success
                        },
                        currentPort = viewModel.currentPort.toString()
                    )
                }
            }
        }
    }
}


@Composable
fun DisplayServerLogs(
    getLogMsg : () -> String,
){

    Box(modifier = Modifier.fillMaxWidth()){
        Text(text = getLogMsg(), textAlign = TextAlign.Center)
    }


}


