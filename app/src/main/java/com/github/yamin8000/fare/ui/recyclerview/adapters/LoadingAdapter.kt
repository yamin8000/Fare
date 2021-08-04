/*
 *     LoadingAdapter.kt Created by Yamin Siahmargooei at 2021/7/1
 *     Fare: find Iran's cities taxi fares
 *     This file is part of Fare.
 *     Copyright (C) 2021  Yamin Siahmargooei
 *
 *     Fare is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Fare is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Fare.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.github.yamin8000.fare.ui.recyclerview.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.yamin8000.fare.R
import com.github.yamin8000.fare.databinding.ShimmerBinding

/**
 * Generic Loading adapter for recyclerview
 *
 * @property viewId id of view like [R.layout.data_place_holder_shimmer]
 * @property items number of dummy items to load
 */
class LoadingAdapter(private val items : Int = 10) :
    RecyclerView.Adapter<LoadingAdapter.LoadingViewHolder>() {
    
    override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : LoadingViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ShimmerBinding.inflate(inflater, parent, false)
        return LoadingViewHolder(binding.root)
    }
    
    override fun onBindViewHolder(holder : LoadingViewHolder, position : Int) {
        /* no-op */
    }
    
    override fun getItemCount() = items
    
    class LoadingViewHolder(view : View) : RecyclerView.ViewHolder(view)
}