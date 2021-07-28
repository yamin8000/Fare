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
import com.github.yamin8000.fare.databinding.FragmentHomeBinding
import com.github.yamin8000.fare.ui.fragment.BaseFragment
import com.github.yamin8000.fare.util.CONSTANTS.CHOOSING_DEFAULT_CITY
import com.github.yamin8000.fare.util.CONSTANTS.CITY_ID
import com.github.yamin8000.fare.util.CONSTANTS.GENERAL_PREFS
import com.github.yamin8000.fare.util.SharedPrefs
import com.github.yamin8000.fare.util.helpers.ErrorHelper.snack
import com.google.android.material.snackbar.Snackbar

class HomeFragment : BaseFragment<FragmentHomeBinding>({ FragmentHomeBinding.inflate(it) }) {
    
    override fun onViewCreated(view : View, savedInstanceState : Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.aboutButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_aboutFragment)
        }
        
        binding.searchCityButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_searchCityFragment)
        }
        
        binding.exitButton.setOnClickListener { findNavController().navigate(R.id.exitNoticeModal) }
        
        binding.myCityButton.setOnClickListener { handleMyCityButton() }
        
        binding.mapButton.setOnClickListener { workInProgress() }
        
        binding.settingsButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
        }
        
        backPressHandler()
    }
    
    private fun handleMyCityButton() {
        context?.let {
            val sharedPrefs = SharedPrefs(it, GENERAL_PREFS)
            val myCityId = sharedPrefs.readString(CITY_ID)
            if (myCityId.isNotEmpty()) {
                val bundle = bundleOf(CITY_ID to myCityId)
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
    
    private fun workInProgress() {
        snack(getString(R.string.work_in_progress))
    }
}