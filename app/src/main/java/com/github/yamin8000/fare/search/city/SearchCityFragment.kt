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
import com.github.yamin8000.fare.util.CONSTANTS.FUZZY_SEARCH_WINDOW
import com.github.yamin8000.fare.util.Utility.handleCrash
import com.github.yamin8000.fare.util.Utility.hideKeyboard
import com.github.yamin8000.fare.util.helpers.ErrorHelper.netError
import com.github.yamin8000.fare.util.helpers.ErrorHelper.netErrorCache
import com.github.yamin8000.fare.util.helpers.ErrorHelper.snack
import com.github.yamin8000.fare.web.Services
import com.github.yamin8000.fare.web.Services.TOP_CITIES_ID
import com.github.yamin8000.fare.web.WEB
import com.github.yamin8000.fare.web.WEB.Companion.async
import com.github.yamin8000.fare.web.WEB.Companion.eqQuery
import com.github.yamin8000.fare.web.WEB.Companion.fromJsonArray
import com.github.yamin8000.fare.web.WEB.Companion.likeQuery
import com.github.yamin8000.fare.web.WEB.Companion.toJsonArray
import com.google.android.material.snackbar.Snackbar

private const val NOT_SELECTED = -1

class SearchCityFragment :
    BaseFragment<FragmentSearchCityBinding>({ FragmentSearchCityBinding.inflate(it) }) {
    
    private val loadingAdapter : LoadingAdapter by lazy(LazyThreadSafetyMode.NONE) {
        LoadingAdapter(R.layout.shimmer_city_search)
    }
    
    private val cityService = WEB().getService<Services.CityService>()
    
    private val emptyAdapter : EmptyAdapter by lazy(LazyThreadSafetyMode.NONE) { EmptyAdapter() }
    
    private var selectedStateId = NOT_SELECTED
    
    private val searchCityAdapter = SearchCityAdapter(this::onCitySelected)
    
    private var didYouMeanThisSnack : Snackbar? = null
    
    override fun onViewCreated(view : View, savedInstanceState : Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            stateSelectorHandler()
            citySearchHandler()
            loadTopCities()
        } catch (exception : Exception) {
            handleCrash(exception)
        }
    }
    
    private fun loadTopCities() {
        binding.cityList.adapter = loadingAdapter
        context?.let {
            val cache = CitiesCache(it)
            val cachedList = cache.readCache().fromJsonArray<CityJoined>() ?: mutableListOf()
            if (cachedList.isNotEmpty() && cache.isCached()) {
                populateCityList(cachedList)
                addToCachedCities(cachedList)
            } else {
                val topCitiesService = WEB().getService<Services.CityService>()
                topCitiesService.searchCity(cityId = TOP_CITIES_ID).async(this, { cities ->
                    if (cities != null && cities.isNotEmpty()) {
                        populateCityList(cities)
                        addToCachedCities(cities)
                    } else binding.cityList.adapter = emptyAdapter
                }) {
                    netError()
                    binding.cityList.adapter = emptyAdapter
                }
            }
        }
    }
    
    private fun stateSelectorHandler() {
        binding.searchStateInput.setStartIconOnClickListener { stateAutoClearIconHandler() }
        context?.let {
            val cache = StatesCache(it)
            val isCached = cache.isCached()
            val cachedList = cache.readCache().fromJsonArray<State>()
            if (isCached && cachedList != null && cachedList.isNotEmpty()) {
                populateStates(cachedList)
            } else {
                val stateService = WEB().getService<Services.StateService>()
                stateService.getAll().async(this, { stateList ->
                    if (stateList != null && stateList.isNotEmpty()) {
                        populateStates(stateList)
                        cache.writeCache(stateList.toJsonArray())
                    }
                }) {
                    netErrorCache()
                    if (cachedList != null && cachedList.isNotEmpty()) populateStates(cachedList)
                }
            }
        }
    }
    
    private fun stateAutoClearIconHandler() {
        if (binding.searchStateEdit.text.toString().isNotEmpty()) {
            selectedStateId = NOT_SELECTED
            binding.searchStateEdit.text.clear()
            binding.cityList.adapter = null
        }
    }
    
    private fun populateStates(stateList : List<State>) {
        context?.let {
            binding.searchStateInput.isEnabled = true
            val adapter = ArrayAdapter(it, R.layout.dropdown_item, stateList)
            binding.searchStateEdit.setAdapter(adapter)
            binding.searchStateEdit.setOnItemClickListener { parent, _, position, _ ->
                val state = parent.getItemAtPosition(position) as State
                selectedStateId = state.id
                searchCityByStateAndName(selectedStateId)
            }
        }
    }
    
    private fun searchCityByStateAndName(stateId : Int) {
        hideKeyboard()
        didYouMeanThisSnack?.dismiss()
        binding.cityList.adapter = loadingAdapter
        val cityName = binding.searchCityEdit.text.toString().trim()
        cityService.searchCity(cityName = cityName.likeQuery(), stateId = "$stateId".eqQuery())
            .async(this, { cityList ->
                if (cityList != null && cityList.isNotEmpty()) {
                    populateCityList(cityList)
                    addToCachedCities(cityList)
                } else loadCachedCities(cities = cityName.windowed(3))
            }) {
                netErrorCache()
                loadCachedCities(cities = cityName.windowed(3))
            }
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
        didYouMeanThisSnack?.dismiss()
        binding.cityList.adapter = loadingAdapter
        val cityName = binding.searchCityEdit.text.toString().trim()
        
        val query = cityName.likeQuery()
        cityService.searchCity(cityName = query).async(this, { cityList ->
            if (cityList != null && cityList.isNotEmpty()) {
                populateCityList(cityList)
                addToCachedCities(cityList)
            } else loadCachedCities(cities = cityName.windowed(FUZZY_SEARCH_WINDOW))
        }) {
            netErrorCache()
            loadCachedCities(cities = cityName.windowed(FUZZY_SEARCH_WINDOW))
        }
    }
    
    private fun addToCachedCities(cityList : List<CityJoined>) {
        context?.let { safeContext ->
            val cache = CitiesCache(safeContext)
            val cachedList = cache.readCache().fromJsonArray<CityJoined>()
            val newSet = mutableSetOf<CityJoined>()
            newSet.addAll(cityList)
            cachedList?.let { newSet.addAll(it) }
            cache.writeCache(newSet.toList().toJsonArray())
        }
    }
    
    private fun loadCachedCities(cityName : String? = null, stateId : Int? = null,
                                 cities : List<String> = mutableListOf()) {
        context?.let { safeContext ->
            val cache = CitiesCache(safeContext)
            val isCached = cache.isCached()
            val cachedList = cache.readCache().fromJsonArray<CityJoined>() ?: mutableListOf()
            var searchCandidates = mutableSetOf<CityJoined>()
            if (isCached && cachedList.isNotEmpty()) {
                if (cityName != null) {
                    searchCandidates.addAll(cachedList.filter { it.name.contains(cityName) })
                }
                if (stateId != null) {
                    searchCandidates.addAll(cachedList.filter { it.state.id == stateId })
                }
                cities.forEach {
                    searchCandidates.addAll(cachedList.filter { city -> city.name.contains(it) })
                }
                if (searchCandidates.isNotEmpty()) {
                    searchCandidates = sortCandidates(searchCandidates.toMutableList(), cities)
                    val first = searchCandidates.first().name
                    showDidYouMeanThisMessage(first)
                }
            } else netError()
            if (searchCandidates.isNotEmpty()) populateCityList(searchCandidates.toList())
            else binding.cityList.adapter = emptyAdapter
        }
    }
    
    private fun sortCandidates(list : MutableList<CityJoined>,
                               terms : List<String>) : MutableSet<CityJoined> {
        val ranks = mutableListOf<Pair<Int, CityJoined>>()
        for (cityJoined in list) {
            val rank = cityJoined.name.windowed(FUZZY_SEARCH_WINDOW).intersect(terms).size
            ranks.add(rank to cityJoined)
        }
        return ranks.sortedByDescending { it.first }.map { it.second }.toMutableSet()
    }
    
    private fun showDidYouMeanThisMessage(first : String) {
        val message = "${
            getString(R.string.did_you_mean_this)
        }: $first"
        didYouMeanThisSnack = snack(message, Snackbar.LENGTH_INDEFINITE)
    }
    
    
    private fun populateCityList(cityList : List<CityJoined>) {
        searchCityAdapter.submitList(cityList)
        binding.cityList.adapter = searchCityAdapter
        
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