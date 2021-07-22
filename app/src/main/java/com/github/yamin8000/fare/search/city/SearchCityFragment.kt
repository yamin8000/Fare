/*
 *     SearchFragment.kt Created by Yamin Siahmargooei at 2021/7/9
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

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.yamin8000.fare.R
import com.github.yamin8000.fare.databinding.FragmentSearchCityBinding
import com.github.yamin8000.fare.model.CityJoined
import com.github.yamin8000.fare.model.State
import com.github.yamin8000.fare.ui.fragment.BaseFragment
import com.github.yamin8000.fare.ui.recyclerview.adapters.EmptyAdapter
import com.github.yamin8000.fare.ui.recyclerview.adapters.LoadingAdapter
import com.github.yamin8000.fare.util.CONSTANTS.CITY_ID
import com.github.yamin8000.fare.util.Utility.handleCrash
import com.github.yamin8000.fare.util.Utility.hideKeyboard
import com.github.yamin8000.fare.util.helpers.ErrorHelper.netError
import com.github.yamin8000.fare.web.Services
import com.github.yamin8000.fare.web.WEB
import com.github.yamin8000.fare.web.WEB.Companion.async
import com.github.yamin8000.fare.web.WEB.Companion.eqQuery
import com.github.yamin8000.fare.web.WEB.Companion.likeQuery

private const val NOT_SELECTED = -1

class SearchCityFragment :
    BaseFragment<FragmentSearchCityBinding>({ FragmentSearchCityBinding.inflate(it) }) {
    
    private val loadingAdapter : LoadingAdapter by lazy(LazyThreadSafetyMode.NONE) {
        LoadingAdapter(R.layout.shimmer_city_search)
    }
    
    private val cityService = WEB().getService(Services.CityService::class.java)
    
    private val emptyAdapter : EmptyAdapter by lazy(LazyThreadSafetyMode.NONE) { EmptyAdapter() }
    
    private var selectedStateId = NOT_SELECTED
    
    override fun onViewCreated(view : View, savedInstanceState : Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            stateSelectorHandler()
            citySearchHandler()
        } catch (exception : Exception) {
            handleCrash(exception)
        }
    }
    
    private fun stateSelectorHandler() {
        binding.searchStateInput.setStartIconOnClickListener {
            if (binding.searchStateEdit.text.toString().isNotEmpty()) {
                selectedStateId = NOT_SELECTED
                binding.searchStateEdit.text.clear()
                binding.cityList.adapter = null
                
                searchCityByName()
            }
        }
        val stateService = WEB().getService(Services.StateService::class.java)
        stateService.getAll().async(this, { stateList ->
            if (stateList != null && stateList.isNotEmpty()) {
                binding.searchStateInput.isEnabled = true
                val safeContext = context
                if (safeContext != null) {
                    val adapter = ArrayAdapter(safeContext, R.layout.dropdown_item, stateList)
                    binding.searchStateEdit.setAdapter(adapter)
                    binding.searchStateEdit.setOnItemClickListener { parent, _, position, _ ->
                        val state = parent.getItemAtPosition(position) as State
                        selectedStateId = state.id
                        searchCityByStateAndName(selectedStateId)
                    }
                }
            }
        }) { netError() }
    }
    
    private fun searchCityByStateAndName(stateId : Int) {
        hideKeyboard()
        
        binding.cityList.adapter = loadingAdapter
        val cityName = binding.searchCityEdit.text.toString().likeQuery()
        cityService.searchCity(cityName = cityName, stateId = "$stateId".eqQuery()).async(this, { cityList ->
            if (cityList != null && cityList.isNotEmpty()) populateCityList(cityList)
            else binding.cityList.adapter = emptyAdapter
        }) { netError() }
    }
    
    private fun citySearchHandler() {
        binding.searchCityInput.setStartIconOnClickListener { searchStateHandler() }
        
        binding.searchCityEdit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) searchStateHandler()
            true
        }
    }
    
    private fun searchStateHandler() {
        if (selectedStateId != NOT_SELECTED) {
            searchCityByStateAndName(selectedStateId)
        } else searchCityByName()
    }
    
    private fun searchCityByName() {
        hideKeyboard()
        
        binding.cityList.adapter = loadingAdapter
        val query = binding.searchCityEdit.text.toString().trim().likeQuery()
        cityService.searchCity(cityName = query).async(this, { cityList ->
            if (cityList != null && cityList.isNotEmpty()) populateCityList(cityList)
            else binding.cityList.adapter = emptyAdapter
        }) { netError() }
    }
    
    private fun populateCityList(cityList : List<CityJoined>) {
        val adapter = SearchCityAdapter(cityList, this::onCitySelected)
        binding.cityList.adapter = adapter
        adapter.notifyDataSetChanged()
        
        if (context != null) {
            val layoutManager = if (cityList.size <= 4) LinearLayoutManager(context)
            else GridLayoutManager(context, 3)
            binding.cityList.layoutManager = layoutManager
        }
    }
    
    private fun onCitySelected(position : Int, city : CityJoined) {
        val cityId = city.id.toString()
        
        val bundle = bundleOf(CITY_ID to cityId)
        findNavController().navigate(R.id.action_searchCityFragment_to_searchLineFragment, bundle)
    }
}