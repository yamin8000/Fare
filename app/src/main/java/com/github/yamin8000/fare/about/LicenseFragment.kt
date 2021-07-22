/*
 *     LicenseFragment.kt Created by Yamin Siahmargooei at 2021/7/1
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

package com.github.yamin8000.fare.about

import android.os.Bundle
import android.view.View
import com.github.yamin8000.fare.R
import com.github.yamin8000.fare.databinding.FragmentLicenseBinding
import com.github.yamin8000.fare.ui.fragment.BaseFragment
import com.github.yamin8000.fare.util.CONSTANTS.LICENSE
import com.github.yamin8000.fare.util.CONSTANTS.LICENSE_PREFS
import com.github.yamin8000.fare.util.SUPABASE.SUPA_BASE_URL
import com.github.yamin8000.fare.util.SharedPrefs
import com.github.yamin8000.fare.util.Utility.handleCrash
import com.github.yamin8000.fare.util.helpers.ErrorHelper.netError
import com.github.yamin8000.fare.util.helpers.ErrorHelper.snack
import com.github.yamin8000.fare.web.Services
import com.github.yamin8000.fare.web.WEB
import com.github.yamin8000.fare.web.WEB.Companion.async

class LicenseFragment : BaseFragment<FragmentLicenseBinding>({ FragmentLicenseBinding.inflate(it) }) {
    
    override fun onViewCreated(view : View, savedInstanceState : Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            val safeContext = context
            if (safeContext != null) {
                val sharedPrefs = SharedPrefs(safeContext, LICENSE_PREFS)
                var licenseText = sharedPrefs.readString(LICENSE)
                if (licenseText.isEmpty()) {
                    WEB(SUPA_BASE_URL).getService(Services.LicenseService::class.java).getLicense()
                        .async(this, { list ->
                            if (list != null && list.isNotEmpty()) {
                                licenseText = list.first().text
                                binding.licenseText.text = licenseText
                                sharedPrefs.write(LICENSE, licenseText)
                            } else snack(getString(R.string.data_empty))
                        }) { netError() }
                } else binding.licenseText.text = licenseText
            }
        } catch (exception : Exception) {
            handleCrash(exception)
        }
    }
}