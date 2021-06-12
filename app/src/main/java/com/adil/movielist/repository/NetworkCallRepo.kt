package com.adil.movielist.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.adil.movielist.baseClass.BaseRepository
import com.adil.movielist.data.DatabaseHelper
import com.adil.movielist.model.MovieDetailsResponse
import com.adil.movielist.model.Search
import com.adil.movielist.retrofit.ApiInterface

class NetworkCallRepo(private val apiInterface: ApiInterface) : BaseRepository() {

    lateinit var databaseHelper: DatabaseHelper
    lateinit var search: LiveData<List<Search>>

    suspend fun getMovieList(movieName: String, pages: Int) =
        safeApiCall { apiInterface.getMovieList(movieName, pages) }


    suspend fun getMovieDetails(imdbId: String): MovieDetailsResponse = safeApiCall {
        apiInterface.getMovieDetails(imdbId)
    }

    private fun initializeDB(context: Context): DatabaseHelper {
        return DatabaseHelper.getDataseClient(context)
    }

    suspend fun insertData(context: Context, search: List<Search>) {

        var databaseHelper = initializeDB(context)
        databaseHelper.movieListDao().insertData(search)

    }

    fun fetchDataFromDatabase(context: Context): LiveData<List<Search>>? {

        databaseHelper = initializeDB(context)

        search = databaseHelper.movieListDao().getMovie()

        return search
    }

    suspend fun nukeTable(context: Context) {
        databaseHelper = initializeDB(context)
        databaseHelper.movieListDao().nukeTable()
    }

}