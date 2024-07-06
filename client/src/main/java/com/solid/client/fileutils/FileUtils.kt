package com.solid.client.fileutils

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.solid.dto.TreeNode
import com.solid.client.utils.Logger

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
