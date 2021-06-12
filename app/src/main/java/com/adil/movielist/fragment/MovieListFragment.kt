package com.adil.movielist.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.adil.movielist.R
import com.adil.movielist.baseClass.BaseFragment
import com.adil.movielist.common.*
import com.adil.movielist.databinding.FragmentMovieListBinding
import com.adil.movielist.model.Search
import com.adil.movielist.repository.NetworkCallRepo
import com.adil.movielist.retrofit.ApiInterface
import com.adil.movielist.retrofit.Resource
import com.adil.movielist.viewModel.MovieListViewModel
import kotlinx.android.synthetic.main.fragment_movie_list.*


class MovieListFragment :
    BaseFragment<MovieListViewModel, FragmentMovieListBinding, NetworkCallRepo>() {

    lateinit var movieList: List<Search?>
    private var isConnected: Boolean = false
    private var movieName = "Batman"

    private lateinit var customMoviesAdapter: CustomMoviesAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadVariable()
        isOnline()
        initObserver()
        fetchDataFromDb()
    }

    private fun initObserver() {
        viewModel.searchMovie(movieName, requireContext())
        viewModel.movieNameLiveData.observe(viewLifecycleOwner, Observer {
            Log.i("Info", "Movie Name = $it")
        })

        viewModel.loadMoreListLiveData.observe(requireActivity(), Observer {
            if (it) {
                Handler(Looper.getMainLooper()).postDelayed({
                    viewModel.loadMore()
                }, 2000)
            }
        })

        viewModel.movieResponse.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Loading -> {
                    binding.progressIndicator.visible(true)
                    binding.rvMovieList.visible(false)
                }
                is Resource.Success -> {
                    binding.progressIndicator.visible(false)
                    binding.rvMovieList.visible(true)
                    fetchDataFromDb()
                }
                is Resource.Error -> {
                    binding.progressIndicator.visible(false)
                    binding.rvMovieList.visible(true)
                    fetchDataFromDb()
                    layoutMain.snackbar(it.message)
                }
            }
        })
    }

    private fun loadVariable() {

        activity?.title = "Movie List"
        customMoviesAdapter = CustomMoviesAdapter()
        val spanCount = resources.getInteger(R.integer.recycler_columns)
        binding.rvMovieList.apply {
            layoutManager = GridLayoutManager(context, spanCount)
            itemAnimator = DefaultItemAnimator()
            adapter = customMoviesAdapter
            addOnItemTouchListener(
                RecyclerItemClickListener(
                    requireContext(),
                    binding.rvMovieList,
                    object : RecyclerItemClickListener.OnItemClickListener {
                        override fun onItemClick(view: View, position: Int) {
                            if (isConnected) {
                                var bundle = bundleOf(
                                    "id" to customMoviesAdapter.getData()[position]!!.imdbID
                                )
                                val movieDetailsFragment = MovieDetailsFragment()
                                movieDetailsFragment.arguments = bundle
                                activity!!.supportFragmentManager.beginTransaction()
                                    .replace(R.id.container, movieDetailsFragment)
                                    .addToBackStack("movieDetailsFragment").commit()
                                activity!!.title = movieList[position]!!.Title
                            } else {
                                snackbar("Please check your internet connection")
                            }

                        }

                        override fun onLongItemClick(view: View, position: Int) {
                        }

                    })
            )
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0) {
                        val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
                        val visibleItemCount = layoutManager!!.childCount
                        val totalItemCount = layoutManager.itemCount
                        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                        viewModel.checkForLoadMoreItems(
                            visibleItemCount,
                            totalItemCount,
                            firstVisibleItemPosition
                        )
                    }
                }

            })

        }

        binding.searchView.isSubmitButtonEnabled = true
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                requireContext().dismissKeyboard(binding.searchView)
                searchView.clearFocus()
                movieName = query
                viewModel.searchMovie(movieName, requireContext())
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })
    }

    private fun fetchDataFromDb() {
        viewModel.fetchDataFromDatabase(requireContext())!!.observe(viewLifecycleOwner, Observer {
            movieList = it
            customMoviesAdapter.setData(movieList)
        })

    }

    private fun isOnline() {
        NetworkConnection.isInternetAvailable(requireContext())
            .observe(viewLifecycleOwner, Observer {
                isConnected = it
                if (!isConnected) {
                    binding.layoutMain.snackbar("Please check your internet connection")
                } else {
                    if (viewModel.movieResponse.value is Resource.Error || customMoviesAdapter.itemCount == 0) {
                        viewModel.getMovieList()
                    }
                }
            })
    }


    override fun getViewModel(): Class<MovieListViewModel> = MovieListViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMovieListBinding = FragmentMovieListBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): NetworkCallRepo =
        NetworkCallRepo(retrofitClient.getAPI(ApiInterface::class.java))

}