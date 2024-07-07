package com.solid.client.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solid.client.utils.Logger
import com.solid.dto.FileStatus
import com.solid.dto.FileTreeScan
import com.solid.dto.TreeNode
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import java.sql.Date
import java.text.SimpleDateFormat


@Composable
fun DrawTree(getTree: ()  -> FileTreeScan?, modifier: Modifier = Modifier){

    getTree()?.let { tree ->

        val scrollState = rememberScrollState()

        Column(modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)

        ) {

            DrawHeader(timeStamp = tree.scanTimeStamp, sizeByte = tree.totalByteSize, scanTime = tree.scanTimeMills)

            DrawTreeNodes(tree = tree.root, offset = 0, mutableSetOf())
        }
    } ?: run {

        OnEmptyResults(message = "Ongoing scan process of the current Chrome's file system will be displayed here")


    }

}

@Composable
fun DrawHeader(
    timeStamp : Long,
    sizeByte: Long,
    scanTime : Long
){

    val sizeKb = sizeByte / 1024
    val date = Date(timeStamp)
    val format = SimpleDateFormat.getDateTimeInstance()
    val strDate =   format.format(date)


    Row {
        Text(text = "Created: $strDate | total size: $sizeKb KB | ScanTimeMLS: $scanTime")
    }

}



@Composable
fun ColumnScope.DrawTreeNodes(tree: TreeNode, offset: Int, parentLines : MutableSet<Int>){

    if(tree.nodes == null) {
        return
    }

    val size = tree.nodes!!.size
    var pointer = 1
    val repeats = offset / 20
    parentLines.add(repeats - 1)

    tree.nodes!!.forEach { (t, u) ->

        val isExpanded = remember {
            mutableStateOf(false)
        }

        Row (modifier = Modifier
            .height(70.dp)
            .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Start){

            repeat(repeats){

                val isFinal = remember {
                    it + 1 == repeats
                }

                Spacer(modifier = Modifier.width(10.dp))
                if(parentLines.contains(it) || isFinal)
                    Box(modifier = Modifier
                        .fillMaxHeight(if (isFinal) 0.5f else 1f)
                        .width(2.dp)
                        .background(Color.Black)
                        .align(Alignment.Top))
                else Spacer(modifier = Modifier.width(2.dp))

                Spacer(modifier = Modifier.width(10.dp))
            }

            Box(modifier = Modifier
                .offset(x = (-10).dp)
                .height(2.dp)
                .width(10.dp)
                .background(Color.Black)
                .align(Alignment.CenterVertically))

            Icon(imageVector = if(u.isFile) Icons.Default.FilePresent else (if (isExpanded.value) Icons.Default.FolderOpen else Icons.Default.Folder),
                contentDescription = null,
                tint = when(u.status){
                    FileStatus.OLD -> Color.Black
                    FileStatus.NEW -> Color.Green
                    FileStatus.MODIFIED -> Color.Magenta
                },
                modifier = Modifier.clickable {
                    isExpanded.value = !isExpanded.value
                })
            
            Spacer(modifier = Modifier.width(6.dp))

            Column {
                Text(text = t)
                if(u.isFile)
                    Text((u.byteSize / 1024).toString() + " KB")
            }

        }
        if(isExpanded.value){
            if(pointer == size){
                parentLines.remove(repeats - 1)
            }
            DrawTreeNodes(tree = u, offset = offset + 20, parentLines = parentLines)
        }

        pointer ++
    }


}