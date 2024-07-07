package com.solid.server.filescanner

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

        val targetUsr = "grep /data/data/"
        val targetRegFiles = "grep REG"
        val minusFonts = "grep -v \".ttf\""
        val minusInaccessible = "grep -v \".(deleted)\""
        val minusLogs = "grep -v LOG"
        val minusTempLocks = "grep -v LOCK"
        val minusApk = "grep -v \".apk\""


        val command = "lsof -p $pids | $minusFonts | $minusInaccessible | $targetUsr | $minusLogs | $minusTempLocks" +
                " while read -r cmd pid usr fd typ dev sz nd pth; do echo \$sz${SEPARATOR}\$pth; done"

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





    class Builder(){

        private var targetUsr = false
        private var targetReg = false
        private var minusFonts = false
        private var minusLogs = false
        private var minusTempLocks = false
        private var minusApk = false


        fun setTargetingUserFiles(flag : Boolean) : Builder {
            targetUsr = flag
            return this
        }

        fun setTargetingRegFiles(flag : Boolean) : Builder {
            targetReg = flag
            return this
        }

        fun excludeFontFiles(flag : Boolean) : Builder {
            minusFonts = flag
            return this
        }

        fun excludeLogFiles(flag : Boolean) : Builder {
            minusLogs = flag
            return this
        }

        fun excludeTempLockFiles(flag : Boolean) : Builder {
            minusTempLocks = flag
            return this
        }

        fun excludeApkFiles(flag : Boolean) : Builder {
            minusApk = flag
            return this
        }

        fun build(){

        }




    }



}