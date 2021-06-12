package com.adil.movielist.retrofit

import com.adil.movielist.model.MovieDetailsResponse
import com.adil.movielist.model.MovieResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


interface ApiInterface {

    @GET("?apikey=e5311742")
    suspend fun getMovieList(
        @Query("s") movieName: String,
        @Query("pages") pages: Int
    ): Response<MovieResponse>

    @GET("?apikey=e5311742")
    suspend fun getMovieDetails(
        @Query("i") imdbId: String
    ): Response<MovieDetailsResponse>

}
