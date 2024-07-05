package com.solid.server.filesarchiver

import android.content.Context
import android.content.SharedPreferences
import com.solid.dto.FileTreeScan
import com.solid.server.data.local.database.ScansDB
import com.solid.server.data.local.database.entities.Archive
import com.solid.server.filescanner.ChromeFilesScanner
import com.solid.server.shell.ShellHelper
import com.solid.server.utils.CURRENT_FILE_SYS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

private const val ARCHIVES_DIR = "/archives"
private const val TREES_DIR =  "/filetrees"

class ChromeFilesArchiverImpl(
    private val app: Context,
    private val shell: ShellHelper,
    private val db: ScansDB,
    private val fileSysPref: SharedPreferences

) : ChromeFilesArchiver {


    init {
        createFoldersIfNotExist()
    }


    private fun createFoldersIfNotExist(){
        val archivesDir = File(app.filesDir.path + ARCHIVES_DIR)
        val treesDir = File(app.filesDir.path + TREES_DIR)

        if(!archivesDir.exists()){
            archivesDir.mkdirs()
        }

        if(!treesDir.exists()){
            treesDir.mkdirs()
        }
    }



    override fun archiveFileSystem(scanResults: ChromeFilesScanner.ScanResults) {

        val filesTreeJson = Json.encodeToString(scanResults.fileTreeScan)

        val filePath = app.filesDir.path + TREES_DIR +  "/" + scanResults.id + ".json"

        File(filePath).writeText(filesTreeJson)

        val archivePath = app.filesDir.path + ARCHIVES_DIR + "/" + scanResults.id + ".tar.gz"

        val filesArg = scanResults.allPaths.joinToString {
            "\"$it\" "
        }

        val cmd = "tar -czvf $archivePath $filesArg"

        shell.execute(cmd)

        val archive = Archive(id = scanResults.id, filesTreePath = filePath, filesArchivePath = archivePath)

        db.addArchive(archive)

    }

    override suspend fun restoreFileSystemFromArchive(archiveId: Long) : Boolean {

        val isSuccess = withContext(Dispatchers.IO){

            db.getArchiveById(archiveId)?.filesArchivePath?.let { path ->

                val currentId = fileSysPref.getLong(CURRENT_FILE_SYS, 0)

                if(currentId == archiveId) return@withContext false

                val killChromeCmd = "pkill -f chrome"

                shell.execute(killChromeCmd)

                val unzipCmd = "tar -xzvf $path -C /"

                shell.execute(unzipCmd)

                fileSysPref.edit().putLong(CURRENT_FILE_SYS, archiveId).apply()

                val startChromeCmd = "am start -n com.android.chrome/com.google.android.apps.chrome.Main"

                shell.execute(startChromeCmd)

                return@withContext true
            }

            return@withContext false
        }

        return isSuccess
    }
}