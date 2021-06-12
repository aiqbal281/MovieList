package com.adil.movielist.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.adil.movielist.model.Search

@Dao
interface DaoAccess {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertData(search: List<Search>)

    @Query("SELECT * FROM MovieTable ")
    fun getMovie(): LiveData<List<Search>>

    @Query("DELETE FROM MovieTable")
   suspend fun nukeTable()
}