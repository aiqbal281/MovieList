package com.adil.movielist.baseClass

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adil.movielist.repository.NetworkCallRepo
import com.adil.movielist.viewModel.MovieDetailsViewModel
import com.adil.movielist.viewModel.MovieListViewModel

class ViewModelFactory(private val repository: BaseRepository) :
    ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MovieListViewModel::class.java) -> MovieListViewModel(
                repository as NetworkCallRepo
            ) as T
            modelClass.isAssignableFrom(MovieDetailsViewModel::class.java) -> MovieDetailsViewModel(
                repository as NetworkCallRepo
            ) as T

            else -> throw IllegalArgumentException("ViewModelClass Not Found")
        }
    }
}