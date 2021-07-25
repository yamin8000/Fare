/*
 *     CityLinesInfoModal.kt Created by Yamin Siahmargooei at 2021/7/17
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

package com.github.yamin8000.fare.search.line.modal

import android.os.Bundle
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.util.LinkifyCompat
import com.github.yamin8000.fare.R
import com.github.yamin8000.fare.databinding.CityLinesInfoModalBinding
import com.github.yamin8000.fare.model.CityExtra
import com.github.yamin8000.fare.model.Reference
import com.github.yamin8000.fare.util.CONSTANTS.CITY_ID
import com.github.yamin8000.fare.util.Utility.handleCrash
import com.github.yamin8000.fare.util.helpers.ErrorHelper.netError
import com.github.yamin8000.fare.web.Services
import com.github.yamin8000.fare.web.WEB
import com.github.yamin8000.fare.web.WEB.Companion.async
import com.github.yamin8000.fare.web.WEB.Companion.eqQuery
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CityLinesInfoModal : BottomSheetDialogFragment() {
    
    private val binding : CityLinesInfoModalBinding by lazy(LazyThreadSafetyMode.NONE) {
        CityLinesInfoModalBinding.inflate(layoutInflater)
    }
    
    private val web : WEB by lazy(LazyThreadSafetyMode.NONE) { WEB() }
    
    override fun onCreateView(inflater : LayoutInflater, container : ViewGroup?, bundle : Bundle?) : View {
        return binding.root
    }
    
    override fun onViewCreated(view : View, savedInstanceState : Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            val args = arguments
            val cityId = arguments?.getString(CITY_ID)
            
            if (args != null && cityId != null) {
                getReferences(cityId)
                getCityExtras(cityId)
            }
        } catch (exception : Exception) {
            handleCrash(exception)
        }
    }
    
    private fun getCityExtras(cityId : String) {
        val service = web.getService<Services.CityExtraService>()
        service.getCityExtra(cityId.eqQuery()).async(this, { list ->
            when {
                list != null && list.isEmpty() -> {
                    addTextLineByLine(getString(R.string.data_empty), binding.cityLinesMoreInfo)
                }
                list != null -> handleExtrasData(list)
                else -> netError()
            }
        }) { netError() }
    }
    
    private fun handleExtrasData(list : List<CityExtra>) {
        for (cityExtra in list) addTextLineByLine(cityExtra.info, binding.cityLinesMoreInfo, false)
    }
    
    private fun getReferences(cityId : String) {
        val service = web.getService<Services.PriceReferenceService>()
        service.getCityReference(cityId.eqQuery()).async(this, { list ->
            when {
                list != null && list.isEmpty() -> {
                    addTextLineByLine(getString(R.string.data_empty), binding.cityLinesReference)
                }
                list != null -> handleReferenceData(list)
                else -> netError()
            }
        }) { netError() }
    }
    
    private fun handleReferenceData(list : List<Reference>) {
        for (item in list) addTextLineByLine(item.data, binding.cityLinesReference)
    }
    
    private fun addTextLineByLine(text : String, textView : TextView, addLinks : Boolean = true) {
        val oldText = textView.text
        textView.text = getString(R.string.line_feed_data, oldText, text).trim()
        if (addLinks) LinkifyCompat.addLinks(textView, Linkify.ALL)
    }
}