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

package com.github.yamin8000.fare2.about

import android.content.Context
import android.os.Bundle
import android.text.util.Linkify
import android.view.View
import androidx.core.text.util.LinkifyCompat
import com.github.yamin8000.fare2.R
import com.github.yamin8000.fare2.cache.Cache
import com.github.yamin8000.fare2.databinding.FragmentLicenseBinding
import com.github.yamin8000.fare2.model.License
import com.github.yamin8000.fare2.ui.fragment.BaseFragment
import com.github.yamin8000.fare2.util.CONSTANTS.LICENSE_PREFS
import com.github.yamin8000.fare2.util.Utility.handleCrash
import com.github.yamin8000.fare2.util.helpers.ErrorHelper.netError
import com.github.yamin8000.fare2.util.helpers.ErrorHelper.snack
import com.github.yamin8000.fare2.web.APIs
import com.github.yamin8000.fare2.web.Web
import com.github.yamin8000.fare2.web.Web.async
import com.github.yamin8000.fare2.web.Web.fromJsonArray
import com.github.yamin8000.fare2.web.Web.toJsonArray

class LicenseFragment :
    BaseFragment<FragmentLicenseBinding>({ FragmentLicenseBinding.inflate(it) }) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            enableLinksInLicensesTextView()
            context?.let { loadLicensesText(it) }
        } catch (exception: Exception) {
            handleCrash(exception)
        }
    }

    private fun enableLinksInLicensesTextView() {
        LinkifyCompat.addLinks(binding.licenseHeader, Linkify.ALL)
    }

    /**
     * Load licenses text from web or cache
     *
     */
    private fun loadLicensesText(context: Context) {
        val licenseCache = Cache(context, LICENSE_PREFS)
        val listOfLicenses = licenseCache.readCache().fromJsonArray<License>() ?: mutableListOf()
        if (listOfLicenses.isNotEmpty()) binding.licenseText.text = createLinedText(listOfLicenses)
        else getLicenseFromServer(licenseCache)
    }

    /**
     * Get license from server
     *
     * @param licenseCache cache is used for writing new data to cache
     */
    private fun getLicenseFromServer(licenseCache: Cache) {
        Web.getAPI<APIs.LicenseAPI>().getAll().async(this, { list ->
            if (list.isNotEmpty()) {
                binding.licenseText.text = createLinedText(list)
                licenseCache.writeCache(cache = list.toJsonArray())
            } else snack(getString(R.string.data_empty))
        }) { netError(it) }
    }

    private fun createLinedText(list: List<License>): String {
        val stringBuilder = StringBuilder()
        list.forEach { licenseItem -> stringBuilder.append(licenseItem.text).append("\n") }
        return "$stringBuilder".trim()
    }
}