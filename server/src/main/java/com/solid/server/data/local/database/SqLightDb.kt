package com.solid.server.data.local.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQuery
import com.solid.server.data.local.database.entities.Archive
import com.solid.server.utils.Logger

class SqLightDb(app: Context) : SQLiteOpenHelper(app, DB_NAME, null , DB_VERSION), ScansDB{

    companion object {

        private const val DB_NAME = "ARCHIVE_DB"
        private const val DB_VERSION = 1
        private const val TABLE_NAME = "ARCHIVES"

        private const val ID = "ID"
        private const val FILES_TREE_PATH = "FILES_TREE_PATH"
        private const val FILES_ARCHIVE_PATH = "FILES_ARCHIVE_PATH"

    }


    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = "CREATE TABLE $TABLE_NAME ($ID INTEGER PRIMARY KEY, $FILES_TREE_PATH TEXT, $FILES_ARCHIVE_PATH TEXT)"

        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }


    override fun addArchive(archive: Archive) : Boolean {

        val db = this.writableDatabase
        val cv = ContentValues()

        cv.apply {
            put(ID, archive.id)
            put(FILES_ARCHIVE_PATH, archive.filesArchivePath)
            put(FILES_TREE_PATH, archive.filesArchivePath)
        }

        val success = db.insert(TABLE_NAME, null, cv)

        return success != -1L
    }


    override fun getAllArchives() : List<Archive> {

        val query = "SELECT * FROM $TABLE_NAME"

        return handleQuery(query)
    }


    override fun deleteAllRows() {

        val query = "DELETE FROM $TABLE_NAME"

        val db = this.writableDatabase
        db.execSQL(query)

    }

    override fun getArchiveById(id : Long) : Archive? {

        val query = "SELECT * FROM $TABLE_NAME WHERE $ID = $id"

        val res = handleQuery(query)

        if(res.isEmpty()) return null

        return res.first()

    }

    override fun getLastArchive() : Archive? {

        val query = "SELECT * FROM $TABLE_NAME ORDER BY $ID DESC LIMIT 1"

        val res = handleQuery(query)

        if(res.isEmpty()) return null

        return res.first()
    }


    private fun handleQuery(query: String) : List<Archive> {

        val result = mutableListOf<Archive>()

        val db = this.readableDatabase

        val cursor = db.rawQuery(query, null)

        with(cursor){

            while (moveToNext()){

                val id = getLong(getColumnIndexOrThrow(ID))
                val tree = getString(getColumnIndexOrThrow(FILES_TREE_PATH))
                val archive = getString(getColumnIndexOrThrow(FILES_ARCHIVE_PATH))

                result.add(Archive(
                    id = id,
                    filesTreePath = tree,
                    filesArchivePath = archive
                ))
            }
        }

        cursor.close()

        return result

    }




}