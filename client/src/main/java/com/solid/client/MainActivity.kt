package com.solid.client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.solid.client.presentation.MainVM
import com.solid.client.ui.AllScansScreen
import com.solid.client.ui.OngoingScanningScreen
import com.solid.client.ui.SettingsDialog
import com.solid.client.ui.navigation.AllScansScreen
import com.solid.client.ui.navigation.OngoingScanningScreen
import com.solid.client.ui.theme.ChromiumBackupsTheme
import com.solid.dto.ServerResponses
import com.solid.client.utils.Logger
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {

            val viewModel by viewModels<MainVM>()
            val memoryInfo = viewModel.memoryStatus.collectAsStateWithLifecycle()
            val isConnectedToServer = viewModel.isConnectedToServer.collectAsStateWithLifecycle()
            val isScanningInProgress = viewModel.isScanningInProgress.collectAsStateWithLifecycle()
            val currentScanningTree = viewModel.currentTree.collectAsStateWithLifecycle()
            val scansList = viewModel.scansList.collectAsStateWithLifecycle()
            val isOpenSettingsDialog = remember { mutableStateOf(false) }

            ChromiumBackupsTheme {
                val navController = rememberNavController()
                var isInListScreen by rememberSaveable {
                    mutableStateOf(false)
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                            DisplayMemoryInfo {
                                memoryInfo.value
                            }
                        },

                            navigationIcon = {
                                if(isInListScreen){
                                    IconButton(onClick = {
                                        isInListScreen = false
                                        navController.navigate(OngoingScanningScreen)
                                    }) {
                                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")

                                    }
                                }
                            },

                            actions = {
                                DisplayConnectionStatus {
                                    isConnectedToServer.value
                                }
                            },
                        )
                    },


                    floatingActionButton = {
                        DisplayFloatingButtons(
                            onClick = {

                                when(it){
                                    ButtonType.SCAN -> {
                                        viewModel.toggleScanning()
                                    }
                                    ButtonType.LIST -> {
                                        Logger.log("CLICKED?")
                                        isInListScreen = true
                                        navController.navigate(AllScansScreen)
                                    }
                                    ButtonType.SETTINGS -> {
                                        isOpenSettingsDialog.value = true
                                    }
                                }

                            },
                            isConnected = isConnectedToServer.value,
                            isScanning = isScanningInProgress.value
                        )
                    },

                ) {

                    NavHost(
                        navController = navController,
                        startDestination = OngoingScanningScreen,
                        modifier = Modifier.padding(it)
                    ) {



                        composable<OngoingScanningScreen>{

                            OngoingScanningScreen {
                                currentScanningTree.value
                            }
                        }

                        composable<AllScansScreen> {

                            AllScansScreen(
                                getScans = { scansList.value },
                                onItemClicked = viewModel::restoreScan
                            )
                        }
                    }
                }

                if(isOpenSettingsDialog.value){

                    SettingsDialog(
                        onDismissRequest = { isOpenSettingsDialog.value = false},
                        onConfirmation = { port, host ->
                            isOpenSettingsDialog.value = false
                            viewModel.updateConfiguration(port = port, host = host) },
                        currentPort = viewModel.currentPort,
                        currentHost = viewModel.currentHost
                    )
                }
            }
        }
    }
}

@Composable
fun DisplayFloatingButtons(
    onClick : (ButtonType) -> Unit,
    isConnected: Boolean,
    isScanning: Boolean
){

    Column() {
        DrawScanningButton(onClick = { onClick(ButtonType.SCAN) }, isConnected = isConnected, isScanning = isScanning)
        Spacer(modifier = Modifier.height(20.dp))
        DrawSettingsButton { onClick(ButtonType.SETTINGS) }
        Spacer(modifier = Modifier.height(20.dp))
        DrawToListButton { onClick(ButtonType.LIST) }
    }
}


@Composable
fun DrawScanningButton(
    onClick : () -> Unit,
    isConnected: Boolean,
    isScanning: Boolean
){
    FloatingActionButton(
        onClick = onClick,
        containerColor = if(!isConnected) Color.Gray else if(isScanning) Color.Green else Color.Red) {
        Icon(imageVector = Icons.Default.Search, contentDescription = "Scan", tint = Color.White)
    }
}

@Composable
fun DrawSettingsButton(
    onClick : () -> Unit,
){
    FloatingActionButton(
        onClick = onClick,
        containerColor = Color.Blue) {
        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
    }
}


@Composable
fun DrawToListButton(
    onClick : () -> Unit,
){
    FloatingActionButton(
        onClick = onClick,
        containerColor = Color.Magenta) {
        Icon(imageVector = Icons.Default.List, contentDescription = "All scans list", tint = Color.White)
    }
}


@Composable
fun DisplayMemoryInfo(
    getMemoryInfo: () -> ServerResponses.MemoryStatus?
){
    val mem = getMemoryInfo()

    Text(text = "PSS: ${mem?.memoryUsageKb ?: "?"} kb | total: ${mem?.availableRamKb ?: "?"}",
        fontSize = 12.sp
        )
}

@Composable
fun DisplayConnectionStatus(
    getStatus: () -> Boolean
){

    SideEffect {
        Logger.log("RECOMPOSING CONNECTION STATUS")
    }

    val isConnected = getStatus()

    Text(text = if(isConnected) "Connected" else "Not connected", color = if(isConnected) Color.Green else Color.Red)
}

enum class ButtonType {
    SCAN, LIST, SETTINGS
}