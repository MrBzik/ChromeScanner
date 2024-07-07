package com.solid.client.ui

import androidx.compose.runtime.Composable

@Composable
fun ArchiveRecoveryPage(
    getMessage : () -> String
){

    OnEmptyResults(message = getMessage())

}