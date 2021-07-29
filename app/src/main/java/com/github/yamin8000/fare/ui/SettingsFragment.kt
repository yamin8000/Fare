/*
 *     Fare: find Iran's cities taxi fares
 *     SettingsFragment.kt Created by Yamin Siahmargooei at 2021/7/28
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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import com.github.yamin8000.fare.databinding.FragmentSettingsBinding
import com.github.yamin8000.fare.ui.fragment.BaseFragment
import com.github.yamin8000.fare.util.CONSTANTS.GENERAL_PREFS
import com.github.yamin8000.fare.util.CONSTANTS.IS_NIGHT_THEME
import com.github.yamin8000.fare.util.SharedPrefs
import com.github.yamin8000.fare.util.Utility.handleCrash

class SettingsFragment : BaseFragment<FragmentSettingsBinding>({ FragmentSettingsBinding.inflate(it) }) {
    
    override fun onCreateView(inflater : LayoutInflater, container : ViewGroup?, bundle : Bundle?) : View {
        return binding.root
    }
    
    override fun onViewCreated(view : View, savedInstanceState : Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            toggleSwitchBasedOnCurrentTheme()
            switchChangeListener()
        } catch (exception : Exception) {
            handleCrash(exception)
        }
    }
    
    private fun switchChangeListener() {
        binding.dayNightSwitch.setOnCheckedChangeListener { _, isChecked ->
            val nightMode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(nightMode)
            saveNightModePreference(isChecked)
        }
    }
    
    private fun saveNightModePreference(isChecked : Boolean) {
        context?.let {
            val sharedPrefs = SharedPrefs(it, GENERAL_PREFS)
            sharedPrefs.write(IS_NIGHT_THEME, isChecked)
        }
    }
    
    private fun toggleSwitchBasedOnCurrentTheme() {
        when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_YES -> binding.dayNightSwitch.isChecked = true
            else -> binding.dayNightSwitch.isChecked = false
        }
    }
}