package com.solid.client.ui

import android.app.Dialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.solid.client.domain.ConfigInputRes

@Composable
fun SettingsDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: (port: String, host: String, interval : String) -> ConfigInputRes,
    currentPort : String,
    currentHost:  String,
    scanningInterval : String
){

    val port = remember {
        mutableStateOf(currentPort)
    }

    val host = remember {
        mutableStateOf(currentHost)
    }

    val scanInterval = remember {
        mutableStateOf(scanningInterval)
    }


    Dialog(onDismissRequest = { onDismissRequest()}) {
        Card(modifier = Modifier
            .fillMaxWidth()
            .height(375.dp)
            .padding(16.dp),
            shape = RoundedCornerShape(16.dp)){
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                TextInputComposable(state = host, onValueChange = {
                    host.value = it
                }, label = "Host")

                TextInputComposable(state = port, onValueChange = {
                    port.value = it
                }, label = "Port")

                TextInputComposable(state = scanInterval, onValueChange = {
                    scanInterval.value = it
                }, label = "Scan interval in seconds")

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(
                        onClick = { onDismissRequest() },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Dismiss")
                    }
                    TextButton(
                        onClick = {
                           val res = onConfirmation(port.value, host.value, scanInterval.value)
                            //TO DO HIGHLIGHT
                                  },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}

@Composable
fun TextInputComposable(
    state : State<String>,
    onValueChange : (String) -> Unit,
    label: String
){

    TextField(
        value = state.value,
        onValueChange = { onValueChange(it) },
        label = {
            Text(text = label)
        }
        )

}