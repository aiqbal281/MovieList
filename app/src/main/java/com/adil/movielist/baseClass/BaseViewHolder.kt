package com.adil.movielist.baseClass

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView.rootView) {

    abstract fun onBind(position: Int)
}