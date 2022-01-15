/*
 *     SearchLineAdapter.kt Created by Yamin Siahmargooei at 2021/7/14
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

package com.github.yamin8000.fare2.search.line

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.github.yamin8000.fare2.databinding.SearchLineItemBinding
import com.github.yamin8000.fare2.model.Line

class SearchLineAdapter :
    RecyclerView.Adapter<SearchLineViewHolder>() {

    private val asyncDiffer: AsyncListDiffer<Line> = AsyncListDiffer(this, DiffCallback)

    private object DiffCallback : DiffUtil.ItemCallback<Line>() {

        override fun areItemsTheSame(oldItem: Line, newItem: Line) = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Line, newItem: Line) = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchLineViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = SearchLineItemBinding.inflate(inflater, parent, false)
        return SearchLineViewHolder(binding, asyncDiffer.currentList, parent.context)
    }

    override fun onBindViewHolder(holder: SearchLineViewHolder, position: Int) {
        val model = asyncDiffer.currentList[position]
        holder.setCode(model.code ?: "", position)
        holder.setOrigin(model.origin ?: "", position)
        holder.setDestination(model.destination ?: "", position)

        val priceList = model.price ?: mutableListOf()
        if (priceList.isNotEmpty()) holder.setPrice(priceList)
    }


    override fun getItemCount() = asyncDiffer.currentList.size

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemViewType(position: Int) = position

    fun submitList(newList: List<Line>) {
        asyncDiffer.submitList(newList)
    }
}