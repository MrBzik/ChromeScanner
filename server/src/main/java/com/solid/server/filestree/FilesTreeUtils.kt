package com.solid.server.filestree

import com.solid.server.utils.Logger


data class GenFileTreeResults(val fileTreeScan: FileTreeScan, val allPaths: Set<String>)

object FilesTreeUtils {

    const val SEPARATOR = ' '

    fun generateTree(
        prevTree : FileTreeScan?,
        filesList: List<String>,
        scanTimeStamp: Long,
        scanTimeMills: Long
    ) : GenFileTreeResults? {

        val forArchive = HashSet<String>()


        val rootTreeNode = TreeNode()

        var isTreeChanged = false
        var totalByteSize = 0L


        filesList.forEach {  line ->

            val splitIdx = line.indexOfFirst { c ->
                c == SEPARATOR
            }

            val path = line.substring(splitIdx + 1 until line.length)

            Logger.log(path)

            if(forArchive.contains(path)) return@forEach

            forArchive.add(path)

            val byteSize = line.substring(0 until splitIdx).toInt()
            totalByteSize += byteSize

            val filePathList = path.substring(1).split("/")

            val status = rootTreeNode.addFile(
                prevFileTreeScan = prevTree,
                fileWithPath = filePathList,
                byteSize = byteSize
            )

            if(status != FileStatus.OLD){
                isTreeChanged = true
            }

        }

        Logger.log(forArchive.size.toString())

        if(isTreeChanged){
            return GenFileTreeResults(
                fileTreeScan = FileTreeScan(
                    root = rootTreeNode,
                    scanTimeStamp = scanTimeStamp,
                    scanTimeMills = scanTimeMills,
                    totalByteSize = totalByteSize
                ),
                allPaths = forArchive
            )
        }

        return null

    }



    private fun TreeNode.addFile(
        prevFileTreeScan : FileTreeScan?,
        fileWithPath : List<String>,
        byteSize : Int
    ) : FileStatus {

        var currentNode : TreeNode? = this
        var prevNode : TreeNode? = prevFileTreeScan?.root
        var pathPointer = 0
        var status = FileStatus.OLD

        while (true){

            if(pathPointer == fileWithPath.size) break

            val name = fileWithPath[pathPointer]

            val isFile = pathPointer == fileWithPath.lastIndex

            if(isFile){

                val prevFile = prevNode?.nodes?.get(name)

                status = if(prevFile?.isFile == true){
                    if(prevFile.byteSize != byteSize) {
                        Logger.log("$name ${prevFile.byteSize} $byteSize")
                        FileStatus.MODIFIED
                    }
                    else FileStatus.OLD
                } else FileStatus.NEW

                currentNode?.nodes?.put(
                    name,
                    TreeNode(
                        status = status,
                        nodes = null,
                        byteSize = byteSize,
                        isFile = true,
                    )
                )
            }
            else if(currentNode?.nodes?.containsKey(name) == false) {
                status = if(prevNode?.nodes?.containsKey(name) == true) FileStatus.OLD
                    else FileStatus.NEW
                currentNode.nodes?.put(
                    name, TreeNode(status = status)
                )
            }

            currentNode = currentNode?.nodes?.get(name)
            prevNode = prevNode?.nodes?.get(name)

            pathPointer ++
        }
        return status

    }

    fun printTree(tree: TreeNode){

        if(tree.nodes == null) return


        tree.nodes.forEach { (t, u) ->

            Logger.log(if(u.isFile) "$t file" else t)

            printTree(u)

        }
    }


}

