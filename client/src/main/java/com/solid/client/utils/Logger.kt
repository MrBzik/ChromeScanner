package com.solid.client.utils

import android.util.Log

object Logger {

    fun log(
        msg : String,
        tag : String = "MY_TAG"
    ){

        Log.d(tag, msg)
    }

}