package com.solid.server.filesarchiver

import android.content.Context
import android.content.SharedPreferences
import com.solid.dto.FileTreeScan
import com.solid.server.data.local.database.ScansDB
import com.solid.server.data.local.database.entities.Archive
import com.solid.server.filescanner.ChromeFilesScanner
import com.solid.server.repositories.ScansRepo
import com.solid.server.shell.ShellHelper
import com.solid.server.utils.CURRENT_FILE_SYS
import com.solid.server.utils.Logger
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
    private val repo: ScansRepo
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

        val file = File(filePath)
        file.writeText(filesTreeJson)

        val archivePath = app.filesDir.path + ARCHIVES_DIR + "/" + scanResults.id + ".tar.gz"

        val filesArg = scanResults.allPaths.joinToString {
            "\"$it\" "
        }

        val cmd = "tar -czvf $archivePath $filesArg"

        shell.execute(cmd)

        val archive = Archive(id = scanResults.id, filesTreePath = filePath, filesArchivePath = archivePath)

        repo.addArchive(archive)

    }

    override suspend fun restoreFileSystemFromArchive(archiveId: Long) : ChromeFilesArchiver.ArchivingRes? {

        val isSuccess = withContext(Dispatchers.IO){

            val startTime = System.currentTimeMillis()

            repo.getArchiveById(archiveId)?.filesArchivePath?.let { path ->

                val currentId = repo.getCurrentFileSystemId()

                if(currentId == archiveId) return@withContext null

                val killChromeCmd = "pkill -f chrome"

                shell.execute(killChromeCmd)

                val unzipCmd = "tar -xzvf $path -C /"

                shell.execute(unzipCmd)

                repo.updateCurrentFileSysId(archiveId)

                val startChromeCmd = "am start -n com.android.chrome/com.google.android.apps.chrome.Main"

                shell.execute(startChromeCmd)

                val finishTime = System.currentTimeMillis()

                return@withContext ChromeFilesArchiver.ArchivingRes(finishTime, finishTime - startTime)
            }

            return@withContext null
        }

        return isSuccess
    }
}