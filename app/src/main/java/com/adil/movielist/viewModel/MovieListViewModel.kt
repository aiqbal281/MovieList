package com.adil.movielist.viewModel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.adil.movielist.baseClass.BaseViewModel
import com.adil.movielist.common.Constant
import com.adil.movielist.model.MovieResponse
import com.adil.movielist.model.Search
import com.adil.movielist.repository.NetworkCallRepo
import com.adil.movielist.retrofit.Resource
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MovieListViewModel(private val networkCallRepo: NetworkCallRepo) : BaseViewModel() {

    var liveDataMovieList: LiveData<List<Search>>? = null
    private lateinit var mContext: Context
    private val _movieResponse = MutableLiveData<Resource<ArrayList<Search?>>>()
    val movieResponse: LiveData<Resource<ArrayList<Search?>>> get() = _movieResponse

    private fun insertData(context: Context, search: List<Search>) = viewModelScope.launch {
        networkCallRepo.insertData(context, search)
    }

    fun fetchDataFromDatabase(context: Context): LiveData<List<Search>>? {
        liveDataMovieList = networkCallRepo.fetchDataFromDatabase(context)
        return liveDataMovieList
    }

    private var pageIndex = 0
    private var totalMovies = 0
    private var movieList = ArrayList<Search?>()
    private val _movieNameLiveData = MutableLiveData<String>()
    val movieNameLiveData: LiveData<String>
        get() = _movieNameLiveData

    private val _loadMoreListLiveData = MutableLiveData<Boolean>()
    val loadMoreListLiveData: LiveData<Boolean>
        get() = _loadMoreListLiveData

    private lateinit var listMovieResponse: MovieResponse

    init {
        _loadMoreListLiveData.value = false
        _movieNameLiveData.value = ""
    }

    fun getMovieList() {
        if (pageIndex == 1) {
            movieList.clear()
            _movieResponse.postValue(Resource.loading())
        } else {
            if (movieList.isNotEmpty() && movieList.last() == null)
                movieList.removeAt(movieList.size - 1)
        }
        val handler = CoroutineExceptionHandler { _, _ ->
            _movieResponse.postValue(Resource.error("Failed Coroutine"))
            _loadMoreListLiveData.value = false
        }
        viewModelScope.launch(handler) {
            if (_movieNameLiveData.value != null && _movieNameLiveData.value!!.isNotEmpty()) {
                listMovieResponse =
                    networkCallRepo.getMovieList(_movieNameLiveData.value!!, pageIndex)
                withContext(Dispatchers.Main) {
                    if (listMovieResponse.Response == Constant.SUCCESS) {
                        if (pageIndex == 1)
                            networkCallRepo.nukeTable(mContext)
                        movieList.addAll(listMovieResponse.Search)
                        insertData(mContext, listMovieResponse.Search)
                        totalMovies = listMovieResponse.totalResults.toInt()
                        _movieResponse.postValue(Resource.success(movieList))
                        _loadMoreListLiveData.value = false
                    } else
                        _movieResponse.postValue(Resource.error(listMovieResponse.Error))
                }
            }

        }
    }

    fun searchMovie(movieName: String, context: Context) {
        this.mContext = context
        _movieNameLiveData.value = movieName
        pageIndex = 1
        totalMovies = 0
        getMovieList()
    }

    fun loadMore() {
        pageIndex++
        getMovieList()
    }

    fun checkForLoadMoreItems(
        visibleItemCount: Int,
        totalItemCount: Int,
        firstVisibleItemPosition: Int
    ) {
        if (!_loadMoreListLiveData.value!! && (totalItemCount < totalMovies)) {
            if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                _loadMoreListLiveData.value = true
            }
        }


    }

}