package com.adil.movielist.fragment

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.adil.movielist.R
import com.adil.movielist.common.visible
import com.adil.movielist.databinding.RowMovieListBinding
import com.adil.movielist.model.Search
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView

class CustomMoviesAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_LOADING = 1
    }

    private var moviesList = ArrayList<Search?>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ITEM) {
            val movieListViewBinding =
                RowMovieListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            MovieViewHolder(movieListViewBinding)
        } else {
            val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_lazy_loading, parent, false)
            LoadingViewHolder(view)
        }

    }

    override fun getItemCount(): Int {
        return moviesList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MovieViewHolder) {
            if (moviesList[position] != null) {
                holder.bindItems(moviesList[position]!!)
            }
        } else if (holder is LoadingViewHolder) {
            holder.showLoadingView()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (moviesList[position] == null) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }

    fun setData(newMoviesList: List<Search?>) {
        if (moviesList.isNotEmpty())
            moviesList.removeAt(moviesList.size - 1)
        moviesList.clear()
        moviesList.addAll(newMoviesList)
        notifyDataSetChanged()
    }

    fun getData() = moviesList

    class MovieViewHolder(movieListViewBinding: RowMovieListBinding?) :
        RecyclerView.ViewHolder(movieListViewBinding!!.root) {

        private val imagePoster: ShapeableImageView = movieListViewBinding!!.imgPoster
        private val textTitle: TextView = movieListViewBinding!!.txtTitle

        @SuppressLint("SetTextI18n")
        fun bindItems(movie: Search) {
            textTitle.text = movie.Year + " " + movie.Type
            Glide.with(imagePoster.context).load(movie.Poster)
                .into(imagePoster)
        }

    }

    class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progress_bar)

        fun showLoadingView() {
            progressBar.visible(true)
        }
    }
}