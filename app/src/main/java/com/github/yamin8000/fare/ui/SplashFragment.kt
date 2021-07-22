/*
 *     SplashFragment.kt Created by Yamin Siahmargooei at 2021/7/5
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
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.os.postDelayed
import androidx.navigation.fragment.findNavController
import com.github.yamin8000.fare.R
import com.github.yamin8000.fare.databinding.FragmentSplashBinding
import com.github.yamin8000.fare.ui.fragment.BaseFragment
import com.orhanobut.logger.Logger

class SplashFragment :
    BaseFragment<FragmentSplashBinding>({ FragmentSplashBinding.inflate(it) }) {
    
    override fun onViewCreated(view : View, savedInstanceState : Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed(1500) {
                findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
            }
        } catch (exception : Exception) {
            Logger.d(exception.stackTraceToString())
        }
    }
}