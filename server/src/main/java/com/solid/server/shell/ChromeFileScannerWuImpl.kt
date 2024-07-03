package com.solid.server.shell

import android.content.Context
import android.util.Log
import com.solid.server.filestree.FileTreeScan
import com.solid.server.filestree.FilesTreeUtils
import com.solid.server.filestree.FilesTreeUtils.SEPARATOR
import com.solid.server.filestree.GenFileTreeResults
import com.solid.server.utils.Logger
import com.topjohnwu.superuser.Shell
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class ChromeFileScannerWuImpl (private val app: Context) : ChromeFilesScanner {


//    private var isToExcludeSystemFiles = true
//    private var isToExcludeFontFiles = true
//    private var isToExcludeSystemFiles = true
//    private var isToExcludeSystemFiles = true
//    private var isToExcludeSystemFiles = true

    private var lastFilesTree : FileTreeScan? = null
    private lateinit var archivesDir : File
    private lateinit var treesDir : File

    init {
        setupShell()
        getLastFileTreeScan()
    }




    private fun getLastFileTreeScan(){

        archivesDir = File(app.filesDir.path + "/archives")
        treesDir = File(app.filesDir.path + "/filetrees")

        if(!archivesDir.exists()){
            archivesDir.mkdirs()
        }

        if(!treesDir.exists()){
            treesDir.mkdirs()
        }

        treesDir.listFiles()?.takeIf { it.isNotEmpty() }?.let {
            val lastTreeFile = it.maxBy { it.name.removeSuffix(".json").toLong() }
            lastFilesTree = Json.decodeFromString<FileTreeScan>(lastTreeFile.readText())
        }

        lastFilesTree?.let {
            FilesTreeUtils.printTree(it.root)
        }


    }

    private fun setupShell(){
        Shell.setDefaultBuilder(Shell.Builder.create()
            .setFlags(Shell.FLAG_MOUNT_MASTER)
        )
    }


    override fun launchScan(): String? {

        val scanTimeStamp = System.currentTimeMillis()

        val pids = getChromeProcPidsArg()

        Logger.log("PIDS: $pids")

        val target = "grep /data/data/"
        val minusFonts = "grep -v \".ttf\""
        val minusInaccessible = "grep -v \".(deleted)\""

        val command = "lsof -p $pids | $minusFonts | $minusInaccessible | $target | while read -r cmd pid usr fd typ dev sz nd pth; do echo \$sz${SEPARATOR}\$pth; done"

        val processesRes = Shell.cmd(command).exec()

        if(processesRes.out.size == 0) return null

        val scanTimeMills = System.currentTimeMillis() - scanTimeStamp

        val fileTreeGenRes = FilesTreeUtils.generateTree(
            prevTree = lastFilesTree,
            filesList = processesRes.out,
            scanTimeMills = scanTimeMills,
            scanTimeStamp = scanTimeStamp
        )

        fileTreeGenRes?.let {

            val id = scanTimeStamp.toString()

            archiveFileSystem(id, fileTreeGenRes)

            return id
        }

        return null
    }


    private fun archiveFileSystem(id : String, fileTreeGenRes: GenFileTreeResults){

        Logger.log("ARCHIVING")

        val filesTreeJson = Json.encodeToString(fileTreeGenRes.fileTreeScan)

        val filePath = treesDir.path + "/" + id + ".json"

        File(filePath).writeText(filesTreeJson)

        val archivePath = archivesDir.path + "/" + id + ".tar.gz"

        val filesArg = fileTreeGenRes.allPaths.joinToString {
            "\"$it\" "
        }

        val cmd = "tar -czvf $archivePath $filesArg"

        val res = Shell.cmd(cmd).exec()

//        res.out.forEach {
//            Logger.log(it)
//        }
//
//        res.err.forEach {
//            Logger.log(it)
//        }
//
//        Logger.log(res.isSuccess.toString())
    }



    override fun restoreFileSystem(archivePath: String) {

        val cmd = "tar -xzvf $archivePath -C /"

        Shell.cmd(cmd).exec()


    }

    private fun getChromeProcPidsArg () : String {

        val pidsArgBuilder = StringBuilder()

        val commandToGetProcIds = "ps -ef | grep chrome | grep -v grep | while read -r user pid rest; do echo \$pid; done"

        val pidsRes = Shell.cmd(commandToGetProcIds).exec()


        pidsRes.out.forEach { pid ->
            pidsArgBuilder
                .append(pid)
                .append(",")
        }


        return pidsArgBuilder.toString()

    }
}