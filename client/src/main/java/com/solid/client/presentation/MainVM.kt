package com.solid.client.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solid.client.data.remote.ServerConnector
import com.solid.client.fileutils.printTree
import com.solid.dto.FileTreeScan
import com.solid.dto.ServerResponses
import com.solid.server.utils.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainVM @Inject constructor(
    private val serverConnector: ServerConnector
) : ViewModel() {


    private val _currentTree : MutableStateFlow<FileTreeScan?> = MutableStateFlow(null)
    val currentTree = _currentTree.asStateFlow()



    init {
        observeServerStatus()
        viewModelScope.launch {
            connectToServer()
        }
    }



    private suspend fun connectToServer(){
        while (true){
            serverConnector.establishConnection()
            delay(1000)
        }
    }


    private fun observeServerStatus(){

        viewModelScope.launch {

            serverConnector.serverResponses.collect { response ->

                when(response){
                    is ServerResponses.MemoryStatus -> {

                    }
                    is ServerResponses.NewScan -> {

                        printTree(response.scan.root, 0)
                        _currentTree.update {
                            response.scan
                        }

                    }
                    is ServerResponses.ScanRecoveryResults -> {


                    }
                    is ServerResponses.ScansList -> {


                    }
                }
            }
        }
    }


}