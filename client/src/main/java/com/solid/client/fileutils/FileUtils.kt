package com.solid.client.fileutils

import com.solid.dto.FileTreeScan
import com.solid.dto.TreeNode
import com.solid.server.utils.Logger

fun printTree(tree: TreeNode){

    if(tree.nodes == null) return

    tree.nodes!!.forEach { (t, u) ->

        Logger.log(if(u.isFile) "$t file" else t)

        printTree(u)

    }
}