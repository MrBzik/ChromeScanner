package com.solid.client.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.solid.dto.FileTreeScan


@Composable
fun OngoingScanningScreen(
    getCurrentScan : () -> FileTreeScan?
) {

    DrawTree(getTree = getCurrentScan,
        Modifier
            .fillMaxSize()
            .padding(30.dp)
    )
}

