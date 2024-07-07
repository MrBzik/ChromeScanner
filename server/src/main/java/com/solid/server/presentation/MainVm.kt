package com.solid.server.presentation

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.solid.server.utils.PORT_CONFIG
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class MainVm @Inject constructor(
    @Named (PORT_CONFIG) private val confPrefs : SharedPreferences
): ViewModel() {

    var currentPort = 23456
        private set

    init {
        currentPort = confPrefs.getInt(PORT_CONFIG, 23456)
    }


    fun updatePort(newPort: String): Boolean {
        val port = newPort.toIntOrNull()
        val max = 65535
        port.takeIf { it != null && it <= max }?.let{
            confPrefs.edit().putInt(PORT_CONFIG, it).apply()
            currentPort = it
        }
        return port != null && port <= max
    }

}