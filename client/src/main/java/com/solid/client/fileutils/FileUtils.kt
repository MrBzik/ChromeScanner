package com.solid.client.fileutils

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.solid.dto.FileTreeScan
import com.solid.dto.TreeNode
import com.solid.server.utils.Logger

fun printTree(tree: TreeNode, level: Int){

    if(tree.nodes == null) {

        val underscore = StringBuilder()

        repeat(level - 1){

            underscore.append("_")
        }

        Logger.log(underscore.toString())

        return
    }

    val stars = StringBuilder()
    repeat(level){
        stars.append(" | ")
    }

    tree.nodes!!.forEach { (t, u) ->

        Logger.log(if(u.isFile) "$stars $t file" else "$stars $t")

        printTree(u, level + 1)

    }
}


@Composable
fun DrawTree(tree: TreeNode, modifier: Modifier = Modifier){

    val scrollState = rememberScrollState()

    Column(modifier = modifier
        .fillMaxSize()
        .verticalScroll(scrollState)

    ) {
        DrawTreeNodes(tree = tree, offset = 0)
    }
}


@Composable
fun ColumnScope.DrawTreeNodes(tree: TreeNode, offset: Int){

    if(tree.nodes == null) {

//        Divider(modifier = Modifier.width(offset.dp), thickness = 2.dp, color = Color.Black)

        return
    }

    val size = tree.nodes!!.size
    var pointer = 1

    tree.nodes!!.forEach { (t, u) ->

        val isExpanded = remember {
            mutableStateOf(false)
        }


//        val isFinalFile = remember {
//            size == pointer && u.isFile
//        }

        val repeats = offset / 20

        Row (modifier = Modifier
            .height(50.dp)
            .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Start){

            repeat(repeats){

                val isFinal = remember {
                    it + 1 == repeats
                }

                Spacer(modifier = Modifier.width(10.dp))
                if(isFinal)
                    Box(modifier = Modifier
                        .fillMaxHeight(if (isFinal) 0.5f else 1f)
                        .width(2.dp)
                        .background(Color.Black)
                        .align(Alignment.Top)
                    )
                else Spacer(modifier = Modifier.width(2.dp))

                    Spacer(modifier = Modifier.width(10.dp))
            }

//            if(u.isFile)
                Box(modifier = Modifier
                    .offset(x = (-10).dp)
                    .height(2.dp)
                    .width(10.dp)
                    .background(Color.Black)
                    .align(Alignment.CenterVertically))

            Icon(imageVector = if(u.isFile) Icons.Default.MailOutline else Icons.Default.Email,
                contentDescription = null, modifier = Modifier.clickable {
                    isExpanded.value = !isExpanded.value
                })

            Text(text = t)

        }
            if(isExpanded.value)
                DrawTreeNodes(tree = u, offset = offset + 20)


        pointer ++

    }


}