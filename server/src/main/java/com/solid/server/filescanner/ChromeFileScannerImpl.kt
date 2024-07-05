package com.solid.server.filescanner

import android.content.Context
import android.content.SharedPreferences
import com.solid.dto.FileTreeScan
import com.solid.server.data.local.database.ScansDB
import com.solid.server.filestreeutils.FilesTreeUtils
import com.solid.server.filestreeutils.FilesTreeUtils.SEPARATOR
import com.solid.server.shell.ShellHelper
import com.solid.server.utils.CURRENT_FILE_SYS
import kotlinx.serialization.json.Json
import java.io.File

class ChromeFileScannerImpl (
    private val shell: ShellHelper,
    private val fileSysPref: SharedPreferences,
    private val db: ScansDB
) : ChromeFilesScanner {

    private var currentFileSystem : FileTreeScan? = null


    init {
        getCurrentFileSystem()
    }


    private fun getCurrentFileSystem(id : Long? = null){

        val currentSystemId = id ?: fileSysPref.getLong(CURRENT_FILE_SYS, 0)

        db.getArchiveById(currentSystemId)?.let {

            File(it.filesTreePath).takeIf { file -> file.exists() }?.let { file ->

                currentFileSystem = Json.decodeFromString<FileTreeScan>(file.readText())

            }
        }
    }


    override fun notifyFileSystemChanged(fileSysId : Long) {
        getCurrentFileSystem(fileSysId)
    }

    override fun launchScan(): ChromeFilesScanner.ScanResults? {

        val scanTimeStamp = System.currentTimeMillis()

        val pids = getChromeProcPidsArg()

        val target = "grep /data/data/"
        val minusFonts = "grep -v \".ttf\""
        val minusInaccessible = "grep -v \".(deleted)\""

        val command = "lsof -p $pids | $minusFonts | $minusInaccessible | $target | while read -r cmd pid usr fd typ dev sz nd pth; do echo \$sz${SEPARATOR}\$pth; done"

        val processesRes = shell.execute(command)

        if(processesRes.isEmpty()) return null

        val scanTimeMills = System.currentTimeMillis() - scanTimeStamp

        val fileTreeGenRes = FilesTreeUtils.generateTree(
            prevTree = currentFileSystem,
            filesList = processesRes,
            scanTimeMills = scanTimeMills,
            scanTimeStamp = scanTimeStamp
        )

        fileTreeGenRes?.let {
            currentFileSystem = it.fileTreeScan
        }

        return fileTreeGenRes
    }



    private fun getChromeProcPidsArg () : String {

        val pidsArgBuilder = StringBuilder()

        val commandToGetProcIds = "ps -ef | grep chrome | grep -v grep | while read -r user pid rest; do echo \$pid; done"

        val pidsRes = shell.execute(commandToGetProcIds)


        pidsRes.forEach { pid ->
            pidsArgBuilder
                .append(pid)
                .append(",")
        }


        return pidsArgBuilder.toString()

    }
}