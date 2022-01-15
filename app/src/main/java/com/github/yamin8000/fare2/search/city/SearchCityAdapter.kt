/*
 *     SearchCityAdapter.kt Created by Yamin Siahmargooei at 2021/7/9
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

package com.github.yamin8000.fare2.search.city

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.github.yamin8000.fare2.databinding.SearchCityItemBinding
import com.github.yamin8000.fare2.model.CityJoined

class SearchCityAdapter(private val clickListener: (String, String) -> Unit) :
    RecyclerView.Adapter<SearchCityViewHolder>() {

    private val asyncDiffer: AsyncListDiffer<CityJoined> = AsyncListDiffer(this, DiffCallback)

    private object DiffCallback : DiffUtil.ItemCallback<CityJoined>() {

        override fun areItemsTheSame(oldItem: CityJoined, newItem: CityJoined) = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: CityJoined, newItem: CityJoined) = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchCityViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = SearchCityItemBinding.inflate(inflater, parent, false)
        return SearchCityViewHolder(binding, clickListener, asyncDiffer)
    }

    override fun onBindViewHolder(holder: SearchCityViewHolder, position: Int) {
        val model = asyncDiffer.currentList[position]
        holder.setCity(model.name)
        holder.setCounty(model.county.name)
        holder.setState(model.state.name)
    }

    override fun getItemCount() = asyncDiffer.currentList.size

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemViewType(position: Int) = position

    fun submitList(newList: List<CityJoined>) {
        asyncDiffer.submitList(newList)
    }
}