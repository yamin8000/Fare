/*
 *     SearchLineFragment.kt Created by Yamin Siahmargooei at 2021/7/14
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
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.github.yamin8000.fare.R
import com.github.yamin8000.fare.databinding.FragmentSearchLineBinding
import com.github.yamin8000.fare.model.CityJoined
import com.github.yamin8000.fare.model.CompactLine
import com.github.yamin8000.fare.model.Line
import com.github.yamin8000.fare.ui.fragment.BaseFragment
import com.github.yamin8000.fare.ui.recyclerview.adapters.EmptyAdapter
import com.github.yamin8000.fare.ui.recyclerview.adapters.LoadingAdapter
import com.github.yamin8000.fare.util.CONSTANTS.CHOOSING_DEFAULT_CITY
import com.github.yamin8000.fare.util.CONSTANTS.CITY_ID
import com.github.yamin8000.fare.util.CONSTANTS.CITY_NAME
import com.github.yamin8000.fare.util.CONSTANTS.DESTINATION
import com.github.yamin8000.fare.util.CONSTANTS.FEEDBACK
import com.github.yamin8000.fare.util.CONSTANTS.GENERAL_PREFS
import com.github.yamin8000.fare.util.CONSTANTS.LIMIT
import com.github.yamin8000.fare.util.CONSTANTS.LINE_CODE
import com.github.yamin8000.fare.util.CONSTANTS.ORIGIN
import com.github.yamin8000.fare.util.CONSTANTS.ROW_LIMIT
import com.github.yamin8000.fare.util.SharedPrefs
import com.github.yamin8000.fare.util.Utility.handleCrash
import com.github.yamin8000.fare.util.Utility.hideKeyboard
import com.github.yamin8000.fare.util.helpers.ErrorHelper.netError
import com.github.yamin8000.fare.util.helpers.ErrorHelper.snack
import com.github.yamin8000.fare.web.APIs
import com.github.yamin8000.fare.web.WEB
import com.github.yamin8000.fare.web.WEB.Companion.async
import com.github.yamin8000.fare.web.WEB.Companion.eqQuery
import com.github.yamin8000.fare.web.WEB.Companion.likeQuery
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchLineFragment :
    BaseFragment<FragmentSearchLineBinding>({ FragmentSearchLineBinding.inflate(it) }) {

    private var isFirstTime = true

    private val web: WEB by lazy(LazyThreadSafetyMode.NONE) { WEB() }

    private val loadingAdapter: LoadingAdapter by lazy(LazyThreadSafetyMode.NONE) { LoadingAdapter(8) }

    private val emptyAdapter: EmptyAdapter by lazy(LazyThreadSafetyMode.NONE) { EmptyAdapter() }

    private var searchParams = mutableMapOf<String, String>()

    private var currentCityId = ""

    private var cityModel: CityJoined? = null

    private val searchLineAdapter = SearchLineAdapter()

    private var rowLimit = ROW_LIMIT

    private var lastRowSize = ROW_LIMIT

    private var recyclerViewBeforeScrollState: Parcelable? = null

    private var pleaseWaitSnackbar: Snackbar? = null

    private val backScope = CoroutineScope(Dispatchers.Default)

    private val ioScope = CoroutineScope(Dispatchers.IO)

    private val codesSet = mutableSetOf<String?>()

    private val originsSet = mutableSetOf<String?>()

    private val destinationsSet = mutableSetOf<String?>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val exceptionHandler =
            CoroutineExceptionHandler { _, throwable -> handleCrash(throwable as Exception) }

        try {
            handleDefaultCityChoosing()

            val cityName = arguments?.getString(CITY_NAME) ?: ""
            setCityNameToToolbarTitle(cityName)

            val cityId = arguments?.getString(CITY_ID) ?: ""
            if (cityId.isNotEmpty()) {
                currentCityId = cityId
                searchParams[CITY_ID] = cityId
                searchParams[LIMIT] = "$rowLimit"

                lifecycleScope.launch(exceptionHandler) { getCityCompactLines(cityId) }
                lifecycleScope.launch(exceptionHandler) { getCityLinesFullInfo() }
                lifecycleScope.launch(exceptionHandler) { getCityInfo(cityId) }
                lifecycleScope.launch(exceptionHandler) { handleMenu(cityId, cityName) }
            } else netError()
        } catch (exception: Exception) {
            handleCrash(exception)
        }
    }

    private fun setCityNameToToolbarTitle(cityName: String) {
        binding.cityLinesToolbarTitle.text = getString(R.string.line_city_name_template, cityName)
    }

    /**
     * Get city compact lines,
     * get only code,origin,destination of line
     *
     * @param cityId
     */
    private fun getCityCompactLines(cityId: String) {
        web.getAPI<APIs.CompactLineApi>().getCityLines(cityId.eqQuery()).async(this, {
            if (it.isNotEmpty()) {
                enableTextInputs()
                searchFilterClearButtonListener()
                searchFilterHandler()
                lifecycleScope.launch { handleAutoCompletes(it) }
            }
        }) { netError() }
    }

    private fun enableTextInputs() {
        binding.lineCodeInput.isEnabled = true
        binding.lineDestinationInput.isEnabled = true
        binding.lineOriginInput.isEnabled = true
    }

    /**
     * Handle default city choosing
     *
     * show message if user is choosing a default city,
     * and this city is selected as the default city
     *
     */
    private fun handleDefaultCityChoosing() {
        val isChoosingDefaultCity = arguments?.getBoolean(CHOOSING_DEFAULT_CITY) ?: false
        if (isChoosingDefaultCity) {
            snack(getString(R.string.city_set_as_current_city), Snackbar.LENGTH_LONG)
        }
    }

    /**
     * List scroll handler
     *
     * create new request each time user scroll to end of the list
     */
    private fun listScrollHandler() {
        binding.cityLineList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val isGettingMoreDataAllowed = inquireFetchingMoreDataAllowance(recyclerView, newState)
                if (isGettingMoreDataAllowed) {
                    rowLimit += ROW_LIMIT
                    searchParams[LIMIT] = "$rowLimit"
                    recyclerViewBeforeScrollState = recyclerView.layoutManager?.onSaveInstanceState()
                    getCityLinesFullInfo()
                }
            }
        })
    }

    /**
     * Inquire fetching more data allowance
     *
     * check whether fetching new data from server allowed,
     * based on list state, current list size and size of the all the data in the server
     *
     * @param recyclerView
     * @param newState recycler view scroll state
     * @return data fetching allowance flag
     */
    private fun inquireFetchingMoreDataAllowance(
        recyclerView: RecyclerView,
        newState: Int
    ): Boolean {
        val isScrollingToEnd = !recyclerView.canScrollVertically(1)
        val isScrollEnded = newState == RecyclerView.SCROLL_STATE_IDLE
        val isAllDataFetched = lastRowSize >= rowLimit
        return isScrollingToEnd && isScrollEnded && isAllDataFetched
    }

    /**
     * FAB click listener,
     * this fab is used for changing between list layout manager or grid/stagger layout manager
     *
     */
    private fun fabClickListener() {
        binding.cityLinesFab.setOnClickListener {
            context?.let {
                var manager = binding.cityLineList.layoutManager
                val drawable: Drawable?

                /**
                 * First visible items,
                 * array has two params because span count is 2,
                 * this is very error-prone if span count is changed
                 */
                val firstVisibleItems = intArrayOf(0, 0)
                if (manager is LinearLayoutManager) {
                    firstVisibleItems[0] = manager.findFirstVisibleItemPosition()
                    manager = StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
                    drawable = ContextCompat.getDrawable(it, R.drawable.ic_list_grid)
                } else {
                    (manager as StaggeredGridLayoutManager).findFirstVisibleItemPositions(firstVisibleItems)
                    manager = LinearLayoutManager(it)
                    drawable = ContextCompat.getDrawable(it, R.drawable.ic_list)
                }
                changeListLayoutManager(manager)
                binding.cityLinesFab.setImageDrawable(drawable)
                binding.cityLineList.scrollToPosition(firstVisibleItems[0])
            }
        }
    }

    /**
     * Change list layout manager
     *
     * @param layoutManager desired layout manager
     */
    private fun changeListLayoutManager(layoutManager: RecyclerView.LayoutManager) {
        binding.cityLineList.layoutManager = layoutManager
    }

    private fun handleMenu(cityId: String, cityName: String) {
        binding.searchCityLinesToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.search_city_line_menu_reference -> {
                    val bundle = bundleOf(CITY_ID to cityId)
                    findNavController().navigate(R.id.action_searchLineFragment_to_cityLinesInfoModal, bundle)
                }
                R.id.search_city_line_menu_report -> cityDataErrorReport()
                R.id.search_city_line_menu_my_city -> setCityAsMyCity(cityId, cityName)
                R.id.search_city_line_menu_jump_up -> {
                    binding.cityLineList.scrollToPosition(0)
                    binding.searchLineAppbar.setExpanded(true, true)
                }
            }
            true
        }
    }

    private fun setCityAsMyCity(cityId: String, cityName: String) = ioScope.launch {
        context?.let { setMyCityDataToSharedPreferences(it, cityId, cityName) }
        snack(getString(R.string.city_set_as_current_city), Snackbar.LENGTH_LONG)
    }

    private fun setMyCityDataToSharedPreferences(
        it: Context,
        cityId: String,
        cityName: String
    ) {
        val sharedPrefs = SharedPrefs(it, GENERAL_PREFS)
        sharedPrefs.write(CITY_ID, cityId)
        sharedPrefs.write(CITY_NAME, cityName)
    }

    /**
     * report city line data error,
     * using feedback fragment
     *
     */
    private fun cityDataErrorReport() {
        cityModel?.let {
            val cityLongName = "${it.id}-${it.name}-${it.county.name}-${it.state.name}"
            val feedbackTemplate = getString(R.string.line_error_feedback_template, cityLongName)
            val bundle = bundleOf(FEEDBACK to feedbackTemplate)
            findNavController().navigate(R.id.action_searchLineFragment_to_feedbackFragment, bundle)
        }
    }

    /**
     * Search filter clear button listener,
     * this button clear input/filters and make a new search
     *
     */
    private fun searchFilterClearButtonListener() {
        binding.lineSearchFilterClear.setOnClickListener {
            it.isEnabled = false

            binding.lineOriginAuto.text.clear()
            binding.lineCodeAuto.text.clear()
            binding.lineDestinationAuto.text.clear()

            searchParams.clear()
            searchParams[CITY_ID] = currentCityId
            searchParams[LIMIT] = "$rowLimit"

            getCityLinesFullInfo()
        }
    }

    /**
     * Get current city lines form server
     *
     */
    private fun getCityLinesFullInfo() {
        pleaseWaitSnackbar = snack(getString(R.string.please_wait))
        hideKeyboard()
        if (isFirstTime) binding.cityLineList.adapter = loadingAdapter

        /**
         * use of **?.** safe call is intentional
         *
         * to pass null value query params for retrofit
         *
         * if that parameter doesn't exist
         */
        val cityIdQuery = searchParams[CITY_ID]?.eqQuery()
        val lineCodeQuery = searchParams[LINE_CODE]?.likeQuery()
        val originQuery = searchParams[ORIGIN]?.likeQuery()
        val destQuery = searchParams[DESTINATION]?.likeQuery()
        val limitQuery = searchParams[LIMIT]

        val service = web.getAPI<APIs.LineAPI>()
        service.getCityLines(
            cityId = cityIdQuery, lineCode = lineCodeQuery, origin = originQuery,
            destination = destQuery, limit = limitQuery
        ).async(this, { list ->
            if (list.isNotEmpty()) {
                populateCityLinesList(list)
                lastRowSize = list.size
            } else {
                snack(getString(R.string.data_empty))
                binding.cityLineList.adapter = emptyAdapter
            }
            pleaseWaitSnackbar?.dismiss()
        }) {
            pleaseWaitSnackbar?.dismiss()
            netError()
        }
    }

    /**
     * Populate city lines list to recycler view
     *
     * @param list list of city lines
     */
    private fun populateCityLinesList(list: List<Line>) {
        searchLineAdapter.submitList(list)

        handleLayoutManager(list.size)
        if (isFirstTime) {
            isFirstTime = false
            handleCustomProperties(list)
            listScrollHandler()
            fabClickListener()
        }
        binding.cityLineList.adapter = searchLineAdapter
    }

    /**
     * Handle layout manager,
     * change layout manager based on data size
     *
     * @param listSize size of the list
     */
    private fun handleLayoutManager(listSize: Int) {
        context?.let {
            val layoutManager: RecyclerView.LayoutManager? = when {
                listSize <= 2 -> LinearLayoutManager(it)
                recyclerViewBeforeScrollState == null -> {
                    StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
                }
                else -> null
            }
            layoutManager?.let { binding.cityLineList.layoutManager = layoutManager }

        }
        binding.cityLineList.layoutManager?.onRestoreInstanceState(recyclerViewBeforeScrollState)
    }

    /**
     * Handle custom properties,
     * search if this city has custom property like taxi meter
     *
     * @param list list of city lines
     */
    private fun handleCustomProperties(list: List<Line>) {
        val hasCustomProperties = list.any { it.hasCustomProperty }
        val noticeText = getString(R.string.taxi_meter_city_notice)
        if (hasCustomProperties) snack(noticeText, Snackbar.LENGTH_LONG)
    }

    /**
     * Handle auto completes,
     * prepare data for autocompletes
     *
     * @param list list of city lines
     */
    private fun handleAutoCompletes(list: List<CompactLine>) = backScope.launch {
        val codes = list.asSequence().filter { !it.code.isNullOrBlank() }.map { it.code }
        val origins = list.asSequence().filter { !it.origin.isNullOrBlank() }.map { it.origin }
        val destinations = list.asSequence().filter { !it.destination.isNullOrBlank() }.map { it.destination }

        codesSet.addAll(codes)
        originsSet.addAll(origins)
        destinationsSet.addAll(destinations)

        lifecycleScope.launch {
            populateAutoComplete(codesSet.toList(), binding.lineCodeAuto)
            populateAutoComplete(originsSet.toList(), binding.lineOriginAuto)
            populateAutoComplete(destinationsSet.toList(), binding.lineDestinationAuto)
        }
    }

    /**
     * Populate auto complete,
     * add data to auto complete
     *
     * @param T data type
     * @param list list of data
     * @param autoCompleteTextView autocomplete that's going to be filled
     */
    private fun <T> populateAutoComplete(
        list: List<T>,
        autoCompleteTextView: AutoCompleteTextView
    ) {
        context?.let {
            val adapter = ArrayAdapter(it, R.layout.dropdown_item, list)
            autoCompleteTextView.setAdapter(adapter)
        }
    }

    // TODO: 2021-08-07 consider refactoring this method
    private fun searchFilterHandler() {
        binding.lineCodeInput.setStartIconOnClickListener {
            filterHandler(LINE_CODE, binding.lineCodeAuto.text.toString())
        }
        binding.lineCodeAuto.setOnItemClickListener { _, _, _, _ ->
            filterHandler(LINE_CODE, binding.lineCodeAuto.text.toString())
        }
        binding.lineCodeAuto.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                filterHandler(LINE_CODE, binding.lineCodeAuto.text.toString())
                binding.lineCodeAuto.dismissDropDown()
            }
            true
        }
        binding.lineCodeAuto.doAfterTextChanged {
            if (it.isNullOrBlank()) {
                searchParams.remove(LINE_CODE)
                getCityLinesFullInfo()
            }
        }

        binding.lineOriginInput.setStartIconOnClickListener {
            filterHandler(ORIGIN, binding.lineOriginAuto.text.toString())
        }
        binding.lineOriginAuto.setOnItemClickListener { _, _, _, _ ->
            filterHandler(ORIGIN, binding.lineOriginAuto.text.toString())
        }
        binding.lineOriginAuto.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                filterHandler(ORIGIN, binding.lineOriginAuto.text.toString())
                binding.lineOriginAuto.dismissDropDown()
            }
            true
        }
        binding.lineOriginAuto.doAfterTextChanged {
            if (it.isNullOrBlank()) {
                searchParams.remove(ORIGIN)
                getCityLinesFullInfo()
            }
        }

        binding.lineDestinationInput.setStartIconOnClickListener {
            filterHandler(DESTINATION, binding.lineDestinationAuto.text.toString())
        }
        binding.lineDestinationAuto.setOnItemClickListener { _, _, _, _ ->
            filterHandler(DESTINATION, binding.lineDestinationAuto.text.toString())
        }
        binding.lineDestinationAuto.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                filterHandler(DESTINATION, binding.lineDestinationAuto.text.toString())
                binding.lineDestinationAuto.dismissDropDown()
            }
            true
        }
        binding.lineDestinationAuto.doAfterTextChanged {
            if (it.isNullOrBlank()) {
                searchParams.remove(DESTINATION)
                getCityLinesFullInfo()
            }
        }
    }

    /**
     * Filter handler, add given filter to params and start a new search
     *
     * @param paramConstant filter parameter name
     * @param searchParam filter parameter content
     */
    private fun filterHandler(paramConstant: String, searchParam: String) {
        binding.lineSearchFilterClear.isEnabled = true
        searchParams[paramConstant] = searchParam
        getCityLinesFullInfo()
    }

    /**
     * Get city's basic info
     */
    private fun getCityInfo(cityId: String) {
        val service = web.getAPI<APIs.CityAPI>()
        service.searchCity(cityId = cityId.eqQuery()).async(this, { list ->
            if (list.isNotEmpty()) this.cityModel = list.first()
        }) { netError() }
    }
}