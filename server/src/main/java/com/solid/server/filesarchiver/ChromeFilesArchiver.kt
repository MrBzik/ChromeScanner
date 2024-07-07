package com.solid.server.filesarchiver

import com.solid.server.filescanner.ChromeFilesScanner


interface ChromeFilesArchiver {

    data class ArchivingRes(val timeStamp: Long, val durationMls: Long, val isSuccess: Boolean, val message: String)

    fun archiveFileSystem(scanResults: ChromeFilesScanner.ScanResults)

    suspend fun restoreFileSystemFromArchive(archiveId: Long) : ArchivingRes

}