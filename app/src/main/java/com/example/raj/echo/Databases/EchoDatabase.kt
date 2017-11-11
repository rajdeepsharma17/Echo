package com.example.raj.echo.Databases

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.raj.echo.Databases.EchoDatabase.staticated.COLUMN_SONG_ARTIST
import com.example.raj.echo.Databases.EchoDatabase.staticated.COLUMN_SONG_PATH
import com.example.raj.echo.Databases.EchoDatabase.staticated.COLUMN_SONG_TITLE
import com.example.raj.echo.Databases.EchoDatabase.staticated.TABLE_NAME
import com.example.raj.echo.Songs

/**
 * Created by Srishty on 11/11/2017.
 */
class EchoDatabase:SQLiteOpenHelper{
    var _songList= ArrayList<Songs>()
    object staticated {
        val DB_NAME = "FavoriteDatabase"
        val TABLE_NAME = "FavoriteTable"
        val COLUMN_ID = "SongID"
        val COLUMN_SONG_TITLE = "SongTitle"
        val COLUMN_SONG_ARTIST = "SongArtist"
        val COLUMN_SONG_PATH = "SongPath"
        var DB_VERSION = 1
    }

    override fun onCreate(sqliteDatabase: SQLiteDatabase?) {
        sqliteDatabase?.execSQL(   "CREATE TABLE " + staticated.TABLE_NAME + "( " + staticated.COLUMN_ID + " INTEGER, " + staticated.COLUMN_SONG_ARTIST + "STRING, "
                + staticated.COLUMN_SONG_TITLE + " STRING, " + staticated.COLUMN_SONG_PATH + " STRING); ")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    constructor(context: Context?, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int) : super(context, name, factory, version)
    constructor(context: Context?) : super(context, staticated.DB_NAME, null, staticated.DB_VERSION)



    fun storeAsFavorite(id: Int?, artist: String?, songTitle: String?, path: String?){
        val db = this.writableDatabase
        var contentvalues = ContentValues()
        contentvalues.put(staticated.COLUMN_ID, id)
        contentvalues.put(staticated.COLUMN_SONG_ARTIST, artist)
        contentvalues.put(staticated.COLUMN_SONG_TITLE, songTitle)
        contentvalues.put(staticated.COLUMN_SONG_PATH, path)
        db.insert(staticated.TABLE_NAME,null,contentvalues)
        db.close()
    }
    fun queryDBList(): ArrayList<Songs>?{
        try {

            val db = this.readableDatabase
            val query_params= "SELECT * FROM " + staticated.TABLE_NAME
            var cSor = db.rawQuery(query_params,null)
            if (cSor.moveToFirst()){
                do{
                    var _id = cSor.getInt(cSor.getColumnIndexOrThrow(staticated.COLUMN_ID))
                    var _artist = cSor.getString(cSor.getColumnIndexOrThrow(staticated.COLUMN_SONG_ARTIST))
                    var _title = cSor.getString(cSor.getColumnIndexOrThrow(staticated.COLUMN_SONG_TITLE))
                    var _songPath = cSor.getString(cSor.getColumnIndexOrThrow(staticated.COLUMN_SONG_PATH))
                    _songList.add(Songs(_id.toLong(),_title,_artist,_songPath,0))
                }while(cSor.moveToNext())
            }else{
                return null
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    return _songList
    }
    fun checkifIdExist(_id:Int): Boolean{
        var storedId = -1090
        val db = this.readableDatabase
        val query_params = "SELECT * FROM " + staticated.TABLE_NAME + " WHERE songId = '$_id'"
        val cSor = db.rawQuery(query_params,null)
        if(cSor.moveToFirst()){
            do {

                storedId = cSor.getInt(cSor.getColumnIndexOrThrow(staticated.COLUMN_ID))

            }while(cSor.moveToNext())
            }else{
            return false
        }
        return storedId != -1090
    }
    fun deletefavorite(_id: Int){
        val db = this.writableDatabase
        db.delete(staticated.TABLE_NAME, staticated.COLUMN_ID + " = " +_id, null)
        db.close()

    }

}