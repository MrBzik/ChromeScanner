package com.solid.server.filesarchiver

import com.solid.server.filescanner.ChromeFilesScanner


interface ChromeFilesArchiver {

    fun archiveFileSystem(scanResults: ChromeFilesScanner.ScanResults)

    suspend fun restoreFileSystemFromArchive(archiveId: Long) : Boolean

}