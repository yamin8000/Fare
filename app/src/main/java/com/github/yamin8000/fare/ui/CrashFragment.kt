/*
 *     CrashFragment.kt Created by Yamin Siahmargooei at 2021/7/11
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
import com.github.yamin8000.fare.databinding.FragmentCrashBinding
import com.github.yamin8000.fare.model.LogModel
import com.github.yamin8000.fare.ui.fragment.BaseFragment
import com.github.yamin8000.fare.util.CONSTANTS.STACKTRACE
import com.github.yamin8000.fare.web.APIs
import com.github.yamin8000.fare.web.WEB
import com.github.yamin8000.fare.web.WEB.asyncResponse
import com.orhanobut.logger.Logger

class CrashFragment : BaseFragment<FragmentCrashBinding>({ FragmentCrashBinding.inflate(it) }) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.emptyAdapterText.setOnClickListener { activity?.finish() }
        binding.crashImage.setOnClickListener { activity?.finish() }
        binding.root.setOnClickListener { activity?.finish() }

        arguments?.let {
            val stacktrace = it.getString(STACKTRACE) ?: ""
            if (stacktrace.isNotBlank()) {
                sendStacktraceToServer(stacktrace)
            }
        }
    }

    private fun sendStacktraceToServer(stacktrace: String) {
        //send it to db
        WEB.getAPI<APIs.LogApi>().createLog(LogModel(stacktrace)).asyncResponse(this,
            {
                Logger.d(it.code())
            },
            {
                Logger.d(it.message)
            })
    }
}