package com.solid.client.domain

import com.solid.dto.TreeNode

data class FileTreeScanPresent(
    val sizeKB : Int,
    val root : TreeNode = TreeNode(),

)
