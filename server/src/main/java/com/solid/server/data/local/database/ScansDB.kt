package com.solid.server.data.local.database

import com.solid.server.data.local.database.entities.Archive

interface ScansDB {


    fun addArchive(archive: Archive) : Boolean

    fun getAllArchives() : List<Archive>

    fun getArchiveById(id : Long) : Archive?

    fun getLastArchive() : Archive?

}