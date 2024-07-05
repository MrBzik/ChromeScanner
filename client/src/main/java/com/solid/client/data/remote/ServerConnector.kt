package com.solid.client.data.remote

interface ServerConnector {


    suspend fun establishConnection()

    suspend fun startScanning(intervalsSec: Int)

    suspend fun stopScanning()

    suspend fun recoverFileSystem(id : String)



}