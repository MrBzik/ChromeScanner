package com.solid.server.repositories

import android.content.SharedPreferences
import com.solid.dto.FileTreeScan
import com.solid.dto.ServerResponses
import com.solid.server.data.local.database.ScansDB
import com.solid.server.data.local.database.entities.Archive
import com.solid.server.utils.CURRENT_FILE_SYS
import kotlinx.serialization.json.Json
import java.io.File

class ScansRepo (
    private val db: ScansDB,
    private val fileSysPref: SharedPreferences
) {

    fun getAllScansListJson() : String? {

        val archives = db.getAllArchives()

        if(archives.isEmpty()) return null

        val treeScans = archives.mapNotNull {
            File(it.filesTreePath).takeIf { file -> file.exists() }?.readText()
        }

        if (treeScans.isEmpty()) return null

        val responseObj = ServerResponses.ScansList(emptyList())
        val responseJs = Json.encodeToString(ServerResponses.serializer(), responseObj)

        val combinedJson = treeScans.joinToString(separator = ",", prefix = "[", postfix = "]")

        return responseJs.replace("[]", combinedJson)

    }

    fun getCurrentFileSystem(id : Long? = null) : FileTreeScan? {

        val currentSystemId = id ?: fileSysPref.getLong(CURRENT_FILE_SYS, 0)

        db.getArchiveById(currentSystemId)?.let {

            File(it.filesTreePath).takeIf { file -> file.exists() }?.let { file ->

                return Json.decodeFromString<FileTreeScan>(file.readText())

            }
        }
        return null
    }

    fun addArchive(archive: Archive){
        db.addArchive(archive)
    }


    fun getArchiveById(id: Long) = db.getArchiveById(id)


    fun getCurrentFileSystemId() = fileSysPref.getLong(CURRENT_FILE_SYS, 0)

    fun updateCurrentFileSysId(id: Long){
        fileSysPref.edit().putLong(CURRENT_FILE_SYS, id).apply()
    }


}