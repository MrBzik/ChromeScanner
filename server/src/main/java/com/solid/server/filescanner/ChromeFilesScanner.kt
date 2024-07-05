package com.solid.server.filescanner

import com.solid.dto.FileTreeScan

interface ChromeFilesScanner {

    data class ScanResults(val id : Long, val fileTreeScan: FileTreeScan, val allPaths: Set<String>)

    fun launchScan() : ScanResults?

    fun notifyFileSystemChanged(fileSysId : Long)

}