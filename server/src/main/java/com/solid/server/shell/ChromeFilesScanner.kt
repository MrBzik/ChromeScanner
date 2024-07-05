package com.solid.server.shell

import com.solid.dto.FileTreeScan
import com.solid.server.data.local.database.entities.Archive

interface ChromeFilesScanner {

    data class ScanRes(val archive: Archive, val treeScan: FileTreeScan)

    fun launchScan() : ScanRes?

    fun restoreFileSystem(archivePath: String)

}