package com.adil.movielist.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "MovieTable")
data class Search(

    @ColumnInfo(name = "poster")
    val Poster: String,
    @ColumnInfo(name = "title")
    val Title: String,
    @ColumnInfo(name = "type")
    val Type: String,
    @ColumnInfo(name = "year")
    val Year: String,
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "imdbID")
    val imdbID: String
)