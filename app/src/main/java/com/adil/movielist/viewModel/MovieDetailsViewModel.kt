package com.adil.movielist.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.adil.movielist.baseClass.BaseViewModel
import com.adil.movielist.model.MovieDetailsResponse
import com.adil.movielist.repository.NetworkCallRepo
import com.adil.movielist.retrofit.Resource
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MovieDetailsViewModel(private val networkCallRepo: NetworkCallRepo) : BaseViewModel() {

    private val _detailsResponse: MutableLiveData<Resource<MovieDetailsResponse>> =
        MutableLiveData()
    val detailsResponse: LiveData<Resource<MovieDetailsResponse>> get() = _detailsResponse

    private lateinit var movieDetailResponse: MovieDetailsResponse

    fun getMovieDetails(imdbId: String) {
        val handler = CoroutineExceptionHandler { _, exception ->
            throw Exception(exception)
        }
        _detailsResponse.postValue(Resource.loading())
        viewModelScope.launch(handler) {
            movieDetailResponse = networkCallRepo.getMovieDetails(imdbId)
            withContext(Dispatchers.Main) {
                _detailsResponse.postValue(Resource.success(movieDetailResponse))
            }
        }
    }

}