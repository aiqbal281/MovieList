package com.adil.movielist.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.adil.movielist.baseClass.BaseFragment
import com.adil.movielist.baseClass.BaseViewHolder
import com.adil.movielist.common.snackbar
import com.adil.movielist.common.visible
import com.adil.movielist.databinding.FragmentMovieDetailsBinding
import com.adil.movielist.databinding.RowRatingListBinding
import com.adil.movielist.model.MovieDetailsResponse
import com.adil.movielist.model.Rating
import com.adil.movielist.repository.NetworkCallRepo
import com.adil.movielist.retrofit.ApiInterface
import com.adil.movielist.retrofit.Resource
import com.adil.movielist.viewModel.MovieDetailsViewModel
import kotlinx.coroutines.launch


class MovieDetailsFragment :
    BaseFragment<MovieDetailsViewModel, FragmentMovieDetailsBinding, NetworkCallRepo>() {

    private lateinit var movieDetailsResponse: MovieDetailsResponse
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    private fun init() {
        binding.rvRating.apply {
            layoutManager = GridLayoutManager(context, 3)
            itemAnimator = DefaultItemAnimator()
        }
        viewModel.getMovieDetails(arguments?.getString("id").toString())
        viewModel.detailsResponse.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    binding.progressIndicator.visible(false)
                    lifecycleScope.launch {
                        movieDetailsResponse = it.data
                        binding.movieDetailsResponse = movieDetailsResponse
                        binding.executePendingBindings()
                        binding.rvRating.adapter =
                            it.data.Ratings.let { it1 -> RatingAdapter(it1) }
                    }
                }
                is Resource.Loading -> {
                    binding.progressIndicator.visible(true)
                }
                is Resource.Error -> {
                    binding.mainLayout.snackbar(it.message)
                }
                else -> Toast(requireContext())
            }
        })

    }

    inner class RatingAdapter(var rating: List<Rating>) : RecyclerView.Adapter<BaseViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
            val rowRatingListBinding = RowRatingListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
            return MovieViewHolder(rowRatingListBinding)

        }

        override fun getItemCount(): Int = rating.size


        override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
            holder.onBind(position)
        }


        inner class MovieViewHolder(private val rowRatingListBinding: RowRatingListBinding?) :
            BaseViewHolder(rowRatingListBinding!!.root) {
            override fun onBind(position: Int) {
                val rating = rating[position]
                rowRatingListBinding!!.ratingModel = rating
                rowRatingListBinding.executePendingBindings()
            }
        }
    }

    override fun getViewModel(): Class<MovieDetailsViewModel> = MovieDetailsViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMovieDetailsBinding = FragmentMovieDetailsBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): NetworkCallRepo =
        NetworkCallRepo(retrofitClient.getAPI(ApiInterface::class.java))
}