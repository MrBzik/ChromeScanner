package com.solid.server.shell

import com.solid.server.utils.Logger
import com.topjohnwu.superuser.Shell

class TopWuShell : ShellHelper {

    init {
        Shell.setDefaultBuilder(
            Shell.Builder.create()
            .setFlags(Shell.FLAG_MOUNT_MASTER)
        )
    }

    override fun execute(command: String): List<String> {


        val exec =  Shell.cmd(command).exec()
        exec.err.forEach {
            Logger.log(it)
        }

        return exec.out
    }
}