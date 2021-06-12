package com.adil.movielist.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.adil.movielist.model.Search

@Database(entities = arrayOf(Search::class), version = 1, exportSchema = false)
abstract class DatabaseHelper : RoomDatabase() {

    abstract fun movieListDao():DaoAccess

    companion object{
        @Volatile
        private var INSTANCE: DatabaseHelper? = null

        fun getDataseClient(context: Context) : DatabaseHelper {

            if (INSTANCE != null) return INSTANCE!!

            synchronized(this) {

                INSTANCE = Room
                    .databaseBuilder(context, DatabaseHelper::class.java, "MovieListDB")
                    .fallbackToDestructiveMigration()
                    .build()

                return INSTANCE!!

            }
        }
    }


}