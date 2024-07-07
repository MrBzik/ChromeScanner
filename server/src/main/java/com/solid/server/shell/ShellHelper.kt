package com.solid.server.shell

import com.topjohnwu.superuser.Shell

interface ShellHelper {

    fun execute(command: String) : Shell.Result

}