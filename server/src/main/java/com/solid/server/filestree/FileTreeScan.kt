package com.solid.server.filestree

import kotlinx.serialization.Serializable

@Serializable
data class FileTreeScan (
    val root : TreeNode = TreeNode(),
    val scanTimeStamp : Long,
    val scanTimeMills: Long,
    val totalByteSize: Long
)

@Serializable
data class TreeNode(
    val status: FileStatus = FileStatus.OLD,
    val isFile: Boolean = false,
    val byteSize : Int = 4096,
    val nodes: HashMap<String, TreeNode>? = hashMapOf(),
)

enum class FileStatus {
    OLD, NEW, MODIFIED
}