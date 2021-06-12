package com.adil.movielist.model

data class MovieResponse(
    val Response: String,
    val Search: List<Search>,
    val totalResults: String,
    val Error: String
)