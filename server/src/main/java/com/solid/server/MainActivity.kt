package com.solid.server

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.solid.server.filestree.FileTreeScan
import com.solid.server.filestree.TreeNode
import com.solid.server.shell.ChromeFilesScanner
import com.solid.server.shell.ChromeFileScannerWuImpl
import com.solid.server.ui.theme.ChromiumBackupsTheme
import com.solid.server.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

//        embeddedServer(factory = CIO, port = 8080, module = Application::module).start()


        val scanner: ChromeFilesScanner = ChromeFileScannerWuImpl(this.applicationContext)

        scanner.launchScan()

        setContent {
            ChromiumBackupsTheme {
                Box (modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                    Text(text = "Hello Server!")
                }
            }
        }
    }
}


