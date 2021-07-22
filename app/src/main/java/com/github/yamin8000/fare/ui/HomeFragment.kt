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
import androidx.navigation.fragment.findNavController
import com.github.yamin8000.fare.R
import com.github.yamin8000.fare.databinding.FragmentHomeBinding
import com.github.yamin8000.fare.ui.fragment.BaseFragment
import com.github.yamin8000.fare.util.helpers.ErrorHelper.snack

class HomeFragment : BaseFragment<FragmentHomeBinding>({ FragmentHomeBinding.inflate(it) }) {
    
    override fun onViewCreated(view : View, savedInstanceState : Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.aboutButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_aboutFragment)
        }
        
        binding.searchButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }
        
        binding.exitButton.setOnClickListener {
            findNavController().navigate(R.id.exitNoticeModal)
        }
        
        binding.myCityButton.setOnClickListener { workInProgress() }
        binding.mapButton.setOnClickListener { workInProgress() }
        binding.settingsButton.setOnClickListener { workInProgress() }
        
        backPressHandler()
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