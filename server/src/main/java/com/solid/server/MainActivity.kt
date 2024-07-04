package com.solid.server

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import com.solid.server.ui.theme.ChromiumBackupsTheme

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
                        Intent(applicationContext, ScanningService::class.java).also {
                            it.action = ServiceActions.CONFIGURE.toString()
                            val testBundle = Bundle().also { bundle ->
                                bundle.putInt(CONFIG_PORT, 8080)
                            }
                            it.putExtras(testBundle)
                            startService(it)
                        }
                    }) {
                        Text(text = "CONFIGURE SERVER")
                    }
                }
            }
        }
    }
}


