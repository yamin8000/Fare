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

package com.github.yamin8000.fare2.search.city

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.yamin8000.fare2.R
import com.github.yamin8000.fare2.cache.Cache
import com.github.yamin8000.fare2.databinding.FragmentSearchCityBinding
import com.github.yamin8000.fare2.model.CityJoined
import com.github.yamin8000.fare2.model.State
import com.github.yamin8000.fare2.ui.fragment.BaseFragment
import com.github.yamin8000.fare2.ui.recyclerview.adapters.EmptyAdapter
import com.github.yamin8000.fare2.ui.recyclerview.adapters.LoadingAdapter
import com.github.yamin8000.fare2.util.CONSTANTS.CHOOSING_DEFAULT_CITY
import com.github.yamin8000.fare2.util.CONSTANTS.CITY_ID
import com.github.yamin8000.fare2.util.CONSTANTS.CITY_NAME
import com.github.yamin8000.fare2.util.CONSTANTS.CITY_PREFS
import com.github.yamin8000.fare2.util.CONSTANTS.FUZZY_SEARCH_WINDOW
import com.github.yamin8000.fare2.util.CONSTANTS.GENERAL_PREFS
import com.github.yamin8000.fare2.util.CONSTANTS.STATE_PREFS
import com.github.yamin8000.fare2.util.SharedPrefs
import com.github.yamin8000.fare2.util.Utility.handleCrash
import com.github.yamin8000.fare2.util.Utility.hideKeyboard
import com.github.yamin8000.fare2.util.helpers.ErrorHelper.netError
import com.github.yamin8000.fare2.util.helpers.ErrorHelper.netErrorCache
import com.github.yamin8000.fare2.util.helpers.ErrorHelper.snack
import com.github.yamin8000.fare2.web.APIs
import com.github.yamin8000.fare2.web.APIs.TOP_CITIES_ID
import com.github.yamin8000.fare2.web.Web
import com.github.yamin8000.fare2.web.Web.async
import com.github.yamin8000.fare2.web.Web.eqQuery
import com.github.yamin8000.fare2.web.Web.fromJsonArray
import com.github.yamin8000.fare2.web.Web.likeQuery
import com.github.yamin8000.fare2.web.Web.toJsonArray
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*

private const val NOT_SELECTED = -1

private const val OPTIMIZED_COUNT_OF_TOP_CITIES = 35

private const val CITY_LIST_GRID_SPAN_COUNT = 3

private const val MINIMUM_COUNT_OF_CITIES_FOR_LIST_LAYOUT = 4

class SearchCityFragment :
    BaseFragment<FragmentSearchCityBinding>({ FragmentSearchCityBinding.inflate(it) }) {

    private val cityAPI: APIs.CityAPI by lazy(LazyThreadSafetyMode.NONE) { Web.getAPI() }

    private val loadingAdapter: LoadingAdapter by lazy(LazyThreadSafetyMode.NONE) { LoadingAdapter() }

    private val emptyAdapter: EmptyAdapter by lazy(LazyThreadSafetyMode.NONE) { EmptyAdapter() }

    private val searchCityAdapter = SearchCityAdapter(this::onCitySelected)

    private var selectedStateId = NOT_SELECTED

    private var didYouMeanThisSnack: Snackbar? = null

    private var backScope = CoroutineScope(Dispatchers.Default)

    private var ioScope = CoroutineScope(Dispatchers.IO)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val exceptionHandler =
            CoroutineExceptionHandler { _, throwable -> handleCrash(throwable as Exception) }

        lifecycleScope.launch(exceptionHandler) { stateSelectorHandler() }
        lifecycleScope.launch(exceptionHandler) { citySearchHandler() }
        lifecycleScope.launch(exceptionHandler) { handleCachedCities() }
    }

    /**
     * Handle cached cities
     *
     * load cached cities if there is any or else load some predefined top cities
     *
     */
    private suspend fun handleCachedCities() {
        binding.cityList.adapter = loadingAdapter
        context?.let {
            val cache = Cache(it, CITY_PREFS)
            val cachedList = withContext(ioScope.coroutineContext) {
                cache.readCache().fromJsonArray<CityJoined>() ?: mutableListOf()
            }
            if (cachedList.isEmpty()) fetchTopCities()
            else populateCityList(cachedList.take(OPTIMIZED_COUNT_OF_TOP_CITIES))
        }
    }

    /**
     * Fetch top/popular cities from server
     *
     * very first run of app after install cache popular cities
     */
    private fun fetchTopCities() {
        val topCitiesAPI = Web.getAPI<APIs.CityAPI>()
        topCitiesAPI.searchCity(cityId = TOP_CITIES_ID).async(this, { cities ->
            if (cities.isNotEmpty()) {
                populateCityList(cities)
                addToCachedCities(cities)
            } else binding.cityList.adapter = emptyAdapter
        }) {
            netError(it)
            binding.cityList.adapter = emptyAdapter
        }
    }

    /**
     * State selector handler
     *
     * handling if data is already cached or needs to be requested from web
     */
    private suspend fun stateSelectorHandler() {
        stateInputClearButtonClickListener()
        context?.let {
            val cache = Cache(it, STATE_PREFS)
            val cachedList = withContext(ioScope.coroutineContext) {
                cache.readCache().fromJsonArray<State>() ?: mutableListOf()
            }
            if (cachedList.isEmpty()) fetchStates(cache)
            else populateStates(cachedList)
        }
    }

    private fun stateInputClearButtonClickListener() {
        binding.searchStateInput.setStartIconOnClickListener {
            if (binding.searchStateEdit.text.toString().isNotEmpty())
                stateAutoClearIconHandler()
        }
    }

    /**
     * Fetch states from server and cache them
     *
     * @param cache states cache
     */
    private fun fetchStates(cache: Cache) {
        val stateService = Web.getAPI<APIs.StateAPI>()
        stateService.getAll().async(this, { stateList ->
            if (stateList.isNotEmpty()) {
                populateStates(stateList)
                ioScope.launch { cache.writeCache(cache = stateList.toJsonArray()) }
            }
        }) { netErrorCache() }
    }

    /**
     * State auto clear icon handler
     *
     * clear state input
     *
     */
    private fun stateAutoClearIconHandler() {
        selectedStateId = NOT_SELECTED
        binding.searchStateEdit.text.clear()
        binding.cityList.adapter = null
        lifecycleScope.launch { handleCachedCities() }
    }

    /**
     * Populate states,
     * fill states auto complete view/dropdown
     *
     * @param stateList list of states
     */
    private fun populateStates(stateList: List<State>) {
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

    /**
     * Search city by state and name
     *
     * @param stateId state id of cities that user wants to search
     */
    private fun searchCityByStateAndName(stateId: Int) {
        hideKeyboard()
        didYouMeanThisSnack?.dismiss()
        binding.cityList.adapter = loadingAdapter
        val cityName = binding.searchCityEdit.text.toString().trim()
        cityAPI.searchCity(cityName = cityName.likeQuery(), stateId = "$stateId".eqQuery())
            .async(this, { cityList ->
                if (cityList.isNotEmpty()) {
                    populateCityList(cityList)
                    addToCachedCities(cityList)
                } else lifecycleScope.launch { loadCachedCities(cityGrams = cityName.windowed(3)) }
            }) {
                netErrorCache()
                lifecycleScope.launch { loadCachedCities(cityGrams = cityName.windowed(3)) }
            }
    }

    /**
     * City search handler
     *
     * search icon click listener and ime search button handler
     *
     */
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
        cityAPI.searchCity(cityName = query).async(this, { cityList ->
            if (cityList.isNotEmpty()) {
                populateCityList(cityList)
                addToCachedCities(cityList)
            } else lifecycleScope.launch {
                loadCachedCities(cityGrams = cityName.windowed(FUZZY_SEARCH_WINDOW))
            }
        }) {
            netErrorCache()
            lifecycleScope.launch {
                loadCachedCities(
                    cityGrams = cityName.windowed(
                        FUZZY_SEARCH_WINDOW
                    )
                )
            }
        }
    }

    /**
     * Add list of cities to cached cities
     *
     * @param cityList a list of cities data
     */
    private fun addToCachedCities(cityList: List<CityJoined>) = ioScope.launch {
        context?.let { safeContext ->
            val cache = Cache(safeContext, CITY_PREFS)
            val setOfCachedCities = (cache.readCache().fromJsonArray<CityJoined>()
                ?: mutableListOf()).toMutableSet()
            setOfCachedCities.addAll(cityList)
            cache.writeCache(cache = setOfCachedCities.toList().toJsonArray())
        }
    }

    /**
     * Load cached cities
     *
     * when there is no data connection or user input a typo this method is called,
     * and data from cache is loaded
     *
     * @param cityName search in cache by city name
     * @param stateId search in cache by state id
     * @param cityGrams is list of search term n-grams where n = 3 like گرگ - رگا - گان where search term is گرگان
     */
    private suspend fun loadCachedCities(
        cityName: String? = null, stateId: Int? = null,
        cityGrams: List<String> = mutableListOf()
    ) = backScope.launch {
        context?.let { safeContext ->
            val cachedList = readAllCachedCities(safeContext)
            var searchCandidates = mutableSetOf<CityJoined>()
            if (cachedList.isNotEmpty()) {
                if (cityName != null) {
                    searchCandidates.addAll(cachedList.filter { it.name.contains(cityName) })
                }
                if (stateId != null) {
                    searchCandidates.addAll(cachedList.filter { it.state.id == stateId })
                }
                //search terms
                cityGrams.forEach {
                    searchCandidates.addAll(cachedList.filter { city -> city.name.contains(it) })
                }
                if (searchCandidates.isNotEmpty()) {
                    searchCandidates = sortCandidates(searchCandidates.toMutableList(), cityGrams)
                    lifecycleScope.launch { handleDidYouMeanThisMessage(searchCandidates) }
                }
            } else lifecycleScope.launch { binding.cityList.adapter = emptyAdapter }
            if (searchCandidates.isNotEmpty()) {
                lifecycleScope.launch { populateCityList(searchCandidates.toList()) }
            } else lifecycleScope.launch { binding.cityList.adapter = emptyAdapter }
        }
    }

    private fun handleDidYouMeanThisMessage(searchCandidates: MutableSet<CityJoined>) {
        val first = searchCandidates.firstOrNull()
        first?.let { showDidYouMeanThisMessage(it.name) }
    }

    /**
     * Read all cached cities from shared preferences
     *
     * @param safeContext non-nullable and safe context
     * @return list of all cached cities in shared preferences
     */
    private suspend fun readAllCachedCities(safeContext: Context): List<CityJoined> {
        val cache = Cache(safeContext, CITY_PREFS)
        val cachedList = withContext(ioScope.coroutineContext) {
            cache.readCache().fromJsonArray<CityJoined>() ?: mutableListOf()
        }
        return cachedList
    }

    /**
     * Sort fuzzy search candidates
     *
     * @param list list of cities used as candidates for fuzzy search
     * @param terms is list of search term n-grams where n = 3 like گرگ - رگا - گان where search term is گرگان
     * @return sorted list of candidates based on their intersection by search term
     */
    private fun sortCandidates(
        list: MutableList<CityJoined>,
        terms: List<String>
    ): MutableSet<CityJoined> {
        val ranks = mutableListOf<Pair<Int, CityJoined>>()
        for (cityJoined in list) {
            val rank = cityJoined.name.windowed(FUZZY_SEARCH_WINDOW).intersect(terms.toSet()).size
            ranks.add(rank to cityJoined)
        }
        return ranks.sortedByDescending { it.first }.map { it.second }.toMutableSet()
    }

    /**
     * Show did you mean this message
     * for fuzzy searching
     * when user put in input طهران
     *
     * show user did you mean تهران
     *
     * based on a rudimentary fuzzy search method
     * @param first first result of fuzzy search, item with best rank
     */
    private fun showDidYouMeanThisMessage(first: String) {
        val message = "${getString(R.string.did_you_mean_this)}: $first"
        didYouMeanThisSnack = snack(message, Snackbar.LENGTH_INDEFINITE)
    }

    /**
     * Populate city list
     * fill recycler-view with given list
     *
     * @param cityList list of city data
     */
    private fun populateCityList(cityList: List<CityJoined>) {
        searchCityAdapter.submitList(cityList)
        binding.cityList.adapter = searchCityAdapter
        context?.let { changeListLayoutManagerBasedOnListSize(cityList.size, it) }
    }

    /**
     * Change recycler view layout manager based on list size
     *
     * for list with items lesser and equal than 4 linear layout manager is used
     *
     * for anything else grid layout manager is used
     *
     * @param cityListSize
     * @param safeContext
     */
    private fun changeListLayoutManagerBasedOnListSize(cityListSize: Int, safeContext: Context) {
        var layoutManager: RecyclerView.LayoutManager =
            GridLayoutManager(context, CITY_LIST_GRID_SPAN_COUNT)
        if (cityListSize <= MINIMUM_COUNT_OF_CITIES_FOR_LIST_LAYOUT)
            layoutManager = LinearLayoutManager(safeContext)
        binding.cityList.layoutManager = layoutManager
    }

    /**
     * On city selected callback method
     * for recycler-view item click
     *
     * @param cityId id of the city that user clicked
     */
    private fun onCitySelected(cityId: String, cityName: String) {
        val isChoosingDefaultCity = handleDefaultCityChoosing(cityId, cityName)

        val bundle = bundleOf(
            CITY_ID to cityId,
            CITY_NAME to cityName,
            CHOOSING_DEFAULT_CITY to isChoosingDefaultCity
        )
        findNavController().navigate(R.id.action_searchCityFragment_to_searchLineFragment, bundle)
    }

    /**
     * Handle default city choosing
     *
     * @param cityId id of city user wants to be default city
     * @return true if user is choosing default city and return false if this is a normal search
     */
    private fun handleDefaultCityChoosing(cityId: String, cityName: String): Boolean {
        arguments?.let {
            val isChoosingDefaultCity = it.getBoolean(CHOOSING_DEFAULT_CITY)
            if (isChoosingDefaultCity) {
                context?.let { safeContext ->
                    writeDefaultCityDataToSharedPrefs(safeContext, cityId, cityName)
                }
            }
            return isChoosingDefaultCity
        }
        return false
    }

    private fun writeDefaultCityDataToSharedPrefs(
        safeContext: Context,
        cityId: String,
        cityName: String
    ) {
        val sharedPrefs = SharedPrefs(safeContext, GENERAL_PREFS)
        sharedPrefs.write(CITY_ID, cityId)
        sharedPrefs.write(CITY_NAME, cityName)
    }
}