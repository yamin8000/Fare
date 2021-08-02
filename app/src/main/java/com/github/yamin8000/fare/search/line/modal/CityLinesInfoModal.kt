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
import com.github.yamin8000.fare.cache.Cache
import com.github.yamin8000.fare.databinding.CityLinesInfoModalBinding
import com.github.yamin8000.fare.model.CityExtra
import com.github.yamin8000.fare.model.PriceReference
import com.github.yamin8000.fare.util.CONSTANTS.CITY_EXTRA_PREFS
import com.github.yamin8000.fare.util.CONSTANTS.CITY_ID
import com.github.yamin8000.fare.util.CONSTANTS.PRICE_REFERENCE_PREFS
import com.github.yamin8000.fare.util.Utility.handleCrash
import com.github.yamin8000.fare.util.helpers.ErrorHelper.netError
import com.github.yamin8000.fare.web.APIs
import com.github.yamin8000.fare.web.WEB
import com.github.yamin8000.fare.web.WEB.Companion.async
import com.github.yamin8000.fare.web.WEB.Companion.eqQuery
import com.github.yamin8000.fare.web.WEB.Companion.fromJsonArray
import com.github.yamin8000.fare.web.WEB.Companion.toJsonArray
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
            val cityId = arguments?.getString(CITY_ID) ?: ""
            
            if (cityId.isNotBlank()) {
                context?.let {
                    val cityExtraCache = Cache(it, CITY_EXTRA_PREFS)
                    val priceReferenceCache = Cache(it, PRICE_REFERENCE_PREFS)
                    getReferences(cityId, priceReferenceCache)
                    getCityExtras(cityId, cityExtraCache)
                }
            }
        } catch (exception : Exception) {
            handleCrash(exception)
        }
    }
    
    private fun getCityExtras(cityId : String, cache : Cache) {
        val cachedCityExtras = cache.readCache(cityId).fromJsonArray<CityExtra>() ?: mutableListOf()
        if (cachedCityExtras.isEmpty()) {
            val service = web.getAPI<APIs.CityExtraAPI>()
            service.getCityExtra(cityId.eqQuery()).async(this, { list ->
                if (list.isNotEmpty()) {
                    handleExtrasData(list)
                    cache.writeCache(cityId, list.toJsonArray())
                } else addTextLineByLine(getString(R.string.data_empty), binding.cityLinesMoreInfo)
            }) { netError() }
        } else handleExtrasData(cachedCityExtras)
    }
    
    private fun handleExtrasData(list : List<CityExtra>) {
        for (cityExtra in list) addTextLineByLine(cityExtra.info, binding.cityLinesMoreInfo, false)
    }
    
    private fun getReferences(cityId : String, cache : Cache) {
        val cachedReferences = cache.readCache(cityId).fromJsonArray<PriceReference>() ?: mutableListOf()
        if (cachedReferences.isEmpty()) {
            val service = web.getAPI<APIs.PriceReferenceAPI>()
            service.getCityReference(cityId.eqQuery()).async(this, { list ->
                if (list.isNotEmpty()) {
                    handleReferenceData(list)
                    cache.writeCache(cityId, list.toJsonArray())
                } else addTextLineByLine(getString(R.string.data_empty), binding.cityLinesReference)
            }) { netError() }
        } else handleReferenceData(cachedReferences)
    }
    
    private fun handleReferenceData(list : List<PriceReference>) {
        for (item in list) addTextLineByLine(item.data, binding.cityLinesReference)
    }
    
    private fun addTextLineByLine(text : String, textView : TextView, addLinks : Boolean = true) {
        val oldText = textView.text
        textView.text = getString(R.string.line_feed_data, oldText, text).trim()
        if (addLinks) LinkifyCompat.addLinks(textView, Linkify.ALL)
    }
}