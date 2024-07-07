package com.solid.server.ui

import androidx.compose.foundation.layout.Arrangement
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

@Composable
fun SettingsDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: (port: String) -> Boolean,
    currentPort : String,
){

    val port = remember {
        mutableStateOf(currentPort)
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

                TextInputComposable(state = port, onValueChange = {
                    port.value = it
                }, label = "Port")

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
                            val res = onConfirmation(port.value)
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