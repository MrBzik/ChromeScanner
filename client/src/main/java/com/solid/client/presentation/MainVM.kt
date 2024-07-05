package com.solid.client.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solid.client.data.remote.ServerConnector
import com.solid.server.utils.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainVM @Inject constructor(
    private val serverConnector: ServerConnector
) : ViewModel() {



    fun initializeConnection() {


        viewModelScope.launch {
            serverConnector.establishConnection()
        }

        viewModelScope.launch {
            while (true){
                delay(2000)
                serverConnector.startScanning()
            }
        }
    }





}