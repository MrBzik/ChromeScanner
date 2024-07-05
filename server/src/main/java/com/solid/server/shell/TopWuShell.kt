package com.solid.server.shell

import com.topjohnwu.superuser.Shell

class TopWuShell : ShellHelper {

    init {
        Shell.setDefaultBuilder(
            Shell.Builder.create()
            .setFlags(Shell.FLAG_MOUNT_MASTER)
        )
    }

    override fun execute(command: String): List<String> {
        return Shell.cmd(command).exec().out
    }
}