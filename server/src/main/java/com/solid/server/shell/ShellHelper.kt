package com.solid.server.shell

interface ShellHelper {

    fun execute(command: String) : List<String>

}