package com.solid.server.data.local.database.entities

data class Archive(
    val id: Long,
    val filesTreePath: String,
    val filesArchivePath: String
)
