/*
 *     SearchLineViewHolder.kt Created by Yamin Siahmargooei at 2021/7/14
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

package com.github.yamin8000.fare.search.line

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.github.yamin8000.fare.R
import com.github.yamin8000.fare.databinding.SearchLineItemBinding
import com.github.yamin8000.fare.model.Line
import com.github.yamin8000.fare.model.Price
import com.github.yamin8000.fare.util.Utility.numFormat

class SearchLineViewHolder(
    private val binding : SearchLineItemBinding, clickListener : (Int, Line) -> Unit,
    private val list : List<Line>,
    private val context : Context,
                          ) : RecyclerView.ViewHolder(binding.root) {
    
    init {
        binding.root.setOnClickListener {
            if (adapterPosition != RecyclerView.NO_POSITION) {
                clickListener(adapterPosition, list[adapterPosition])
            }
        }
    }
    
    fun setCode(code : String, position : Int) {
        if (code.isNotBlank()) {
            val text = if (list[position].hasCustomProperty) code
            else getString(R.string.line_code_template, code)
            binding.lineCode.text = text
        } else binding.lineCode.visibility = View.GONE
    }
    
    fun setOrigin(origin : String, position : Int) {
        if (origin.isNotBlank()) {
            val text = if (list[position].hasCustomProperty) origin
            else getString(R.string.line_origin_template, origin)
            binding.lineOrigin.text = text
        } else binding.lineOrigin.visibility = View.GONE
    }
    
    fun setDestination(destination : String, position : Int) {
        if (destination.isNotBlank()) {
            val text = if (list[position].hasCustomProperty) destination
            else getString(R.string.line_destination_template, destination)
            binding.lineDestination.text = text
        } else binding.lineDestination.visibility = View.GONE
    }
    
    fun setPrice(prices : List<Price>) {
        val priceStringBuilder = StringBuilder()
        for (price in prices) {
            val priceName = price.name ?: ""
            if (price.value != "0") {
                var priceFormatted = price.value.numFormat()
                if (priceFormatted != price.value) {
                    priceFormatted = getString(R.string.rial_template, priceFormatted)
                }
                priceStringBuilder.append("$priceName $priceFormatted\n")
            }
        }
        binding.linesPrices.text = getString(R.string.line_price_template, "$priceStringBuilder".trim())
    }
    
    private fun getString(resId : Int, vararg formatArgs : Any) = context.getString(resId, *formatArgs)
}