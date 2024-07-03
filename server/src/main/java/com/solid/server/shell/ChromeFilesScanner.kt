package com.solid.server.shell

interface ChromeFilesScanner {

    fun launchScan() : String?

    fun restoreFileSystem(archivePath: String)

}