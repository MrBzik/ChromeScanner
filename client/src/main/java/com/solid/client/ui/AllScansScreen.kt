package com.solid.client.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.solid.dto.FileTreeScan
import java.sql.Date
import java.text.SimpleDateFormat


@Composable
fun AllScansScreen(
    getScans : () -> List<FileTreeScan>,
    onItemClicked : (id: Long) -> Unit
){

    getScans().takeIf { it.isNotEmpty() }?.let { scans ->

        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {


            scans.forEach {

                DisplayScanItem(scan = it){
                    onItemClicked(it.scanTimeStamp)
                }
            }
        }
    } ?: run {

        OnEmptyResults(message = "The list of stored archives will be displayed here")

    }
}


@Composable
fun DisplayScanItem(
    scan : FileTreeScan,
    onClick : () -> Unit
){



    val sizeKb = scan.totalByteSize / 1024
    val date = Date(scan.scanTimeStamp)
    val format = SimpleDateFormat.getDateTimeInstance()
    val strDate =   format.format(date)


    Row (modifier = Modifier
        .fillMaxWidth()
        .padding(5.dp)
        .border(width = 1.dp, color = Color.Black)
        .padding(8.dp)
        .height(50.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween) {


        Text(text = "$strDate | $sizeKb KB | ScanTMS : ${scan.scanTimeMills}",
            modifier = Modifier.weight(1f))

        IconButton(onClick = { onClick()}) {
            Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Restore")
        }
    }
}

@Composable
@Preview
fun SeeItem(
    strDate : String = "this is the ddddddddddddddddddddddddddddddate",
    sizeKB : Int = 200,
    scanTimeMills : Long = 200L
){
    Row (modifier = Modifier
        .fillMaxWidth()
        .padding(5.dp)
        .border(width = 1.dp, color = Color.Black)
        .padding(8.dp)
        .height(50.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween) {

        Text(text = "$strDate | $sizeKB KB | ScanTMS : ${scanTimeMills}", modifier = Modifier.weight(1f))

        IconButton(onClick = { }) {
            Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Restore")
        }
    }
}