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

@file:Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")

package com.github.yamin8000.fare.search.line

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.yamin8000.fare.databinding.SearchLineItemBinding
import com.github.yamin8000.fare.model.Line
import com.github.yamin8000.fare.ui.recyclerview.other.UniqueItem
import com.github.yamin8000.fare.ui.recyclerview.other.UniqueItemDelegate

class SearchLineAdapter(
    private val list : List<Line>,
    private val clickListener : (Int, Line) -> Unit,
                       ) : RecyclerView.Adapter<SearchLineViewHolder>(),
    UniqueItem by UniqueItemDelegate(list.size) {
    
    override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : SearchLineViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = SearchLineItemBinding.inflate(inflater, parent, false)
        return SearchLineViewHolder(binding, clickListener, list, parent.context)
    }
    
    override fun onBindViewHolder(holder : SearchLineViewHolder, position : Int) {
        val model = list[position]
        holder.setCode(model.code ?: "", position)
        holder.setOrigin(model.origin ?: "", position)
        holder.setDestination(model.destination ?: "", position)
        
        val priceList = model.price
        if (priceList != null && priceList.isNotEmpty()) holder.setPrice(priceList)
    }
}