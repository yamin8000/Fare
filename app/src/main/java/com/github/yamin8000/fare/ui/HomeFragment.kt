/*
 *     HomeFragment.kt Created by Yamin Siahmargooei at 2021/7/1
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

package com.github.yamin8000.fare.ui

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.github.yamin8000.fare.R
import com.github.yamin8000.fare.cache.Cache
import com.github.yamin8000.fare.cache.CachePolicy
import com.github.yamin8000.fare.databinding.FragmentHomeBinding
import com.github.yamin8000.fare.ui.fragment.BaseFragment
import com.github.yamin8000.fare.util.CONSTANTS.CHOOSING_DEFAULT_CITY
import com.github.yamin8000.fare.util.CONSTANTS.CITY_EXTRA_PREFS
import com.github.yamin8000.fare.util.CONSTANTS.CITY_ID
import com.github.yamin8000.fare.util.CONSTANTS.CITY_NAME
import com.github.yamin8000.fare.util.CONSTANTS.CITY_PREFS
import com.github.yamin8000.fare.util.CONSTANTS.GENERAL_PREFS
import com.github.yamin8000.fare.util.CONSTANTS.LICENSE_PREFS
import com.github.yamin8000.fare.util.CONSTANTS.PRICE_REFERENCE_PREFS
import com.github.yamin8000.fare.util.CONSTANTS.STATE_PREFS
import com.github.yamin8000.fare.util.SharedPrefs
import com.github.yamin8000.fare.util.Utility.handleCrash
import com.github.yamin8000.fare.util.helpers.ErrorHelper.netErrorCache
import com.github.yamin8000.fare.util.helpers.ErrorHelper.snack
import com.github.yamin8000.fare.web.APIs
import com.github.yamin8000.fare.web.Web
import com.github.yamin8000.fare.web.Web.asyncResponse
import com.google.android.material.snackbar.Snackbar

class HomeFragment : BaseFragment<FragmentHomeBinding>({ FragmentHomeBinding.inflate(it) }) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            handleButtonClickListeners()
            backPressHandler()
            handleFreshnessOfCache()
        } catch (exception: Exception) {
            handleCrash(exception)
        }
    }

    private fun handleButtonClickListeners() {
        binding.aboutButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_aboutFragment)
        }

        binding.searchCityButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_searchCityFragment)
        }

        binding.myCityButton.setOnClickListener { handleMyCityButton() }

        binding.settingsButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
        }
    }

    private fun handleFreshnessOfCache() {
        //checking if server is responsive
        val service = Web.getAPI<APIs.StateAPI>()
        service.getCount().asyncResponse(this, {
            //remove data if user has access to server
            if (it.code() == 200) clearOldCache()
        }) { netErrorCache() }
    }

    /**
     * Clear old cache if data is not fresh
     *
     */
    private fun clearOldCache() {
        context?.let { safeContext ->
            listOf(
                Cache(safeContext, CITY_PREFS),
                Cache(safeContext, STATE_PREFS, CachePolicy.MonthlyCache),
                Cache(safeContext, LICENSE_PREFS, CachePolicy.MonthlyCache),
                Cache(safeContext, CITY_EXTRA_PREFS),
                Cache(safeContext, PRICE_REFERENCE_PREFS)
            ).forEach { cache -> if (!cache.isDataFresh()) cache.sharedPrefs.clearData() }
        }
    }

    private fun handleMyCityButton() {
        context?.let {
            val sharedPrefs = SharedPrefs(it, GENERAL_PREFS)
            val myCityId = sharedPrefs.readString(CITY_ID)
            val myCityName = sharedPrefs.readString(CITY_NAME)
            if (myCityId.isNotEmpty()) {
                val bundle = bundleOf(CITY_ID to myCityId, CITY_NAME to myCityName)
                findNavController().navigate(R.id.action_homeFragment_to_searchLineFragment, bundle)
            } else {
                snack(getString(R.string.no_my_city_added_yet), Snackbar.LENGTH_INDEFINITE)
                val bundle = bundleOf(CHOOSING_DEFAULT_CITY to true)
                findNavController().navigate(R.id.action_homeFragment_to_searchCityFragment, bundle)
            }
        }
    }

    private fun backPressHandler() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.exitNoticeModal)
            }
        }
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, callback)
    }
}