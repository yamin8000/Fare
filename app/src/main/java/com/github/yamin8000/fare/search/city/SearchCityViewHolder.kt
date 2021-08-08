/*
 *     SearchCityViewHolder.kt Created by Yamin Siahmargooei at 2021/7/11
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

package com.github.yamin8000.fare.search.city

import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.github.yamin8000.fare.databinding.SearchCityItemBinding
import com.github.yamin8000.fare.model.CityJoined

class SearchCityViewHolder(
    private val binding : SearchCityItemBinding,
    clickListener : (String, String) -> Unit, asyncListDiffer : AsyncListDiffer<CityJoined>,
                          ) : RecyclerView.ViewHolder(binding.root) {
    
    init {
        binding.root.setOnClickListener {
            if (adapterPosition != RecyclerView.NO_POSITION) {
                val cityId = asyncListDiffer.currentList[adapterPosition].id.toString()
                val cityName = asyncListDiffer.currentList[adapterPosition].name
                clickListener(cityId, cityName)
            }
        }
    }
    
    
    fun setCity(cityName : String) {
        binding.cityName.text = cityName
    }
    
    fun setState(stateName : String) {
        binding.stateName.text = stateName
    }
    
    fun setCounty(countyName : String) {
        binding.countyName.text = countyName
    }
}