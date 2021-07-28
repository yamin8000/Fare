/*
 *     MyApp.kt Created by Yamin Siahmargooei at 2021/7/1
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

@file:Suppress("unused")

package com.github.yamin8000.fare.util

import android.app.Application
import android.util.Log
import com.github.yamin8000.fare.util.CONSTANTS.CITY
import com.github.yamin8000.fare.util.CONSTANTS.CITY_PREFS
import com.github.yamin8000.fare.util.CONSTANTS.FEEDBACK
import com.github.yamin8000.fare.util.CONSTANTS.FEEDBACK_PREFS
import com.github.yamin8000.fare.util.CONSTANTS.GENERAL
import com.github.yamin8000.fare.util.CONSTANTS.GENERAL_PREFS
import com.github.yamin8000.fare.util.CONSTANTS.LICENSE
import com.github.yamin8000.fare.util.CONSTANTS.LICENSE_PREFS
import com.github.yamin8000.fare.util.CONSTANTS.LOG_TAG
import com.github.yamin8000.fare.util.CONSTANTS.STATE
import com.github.yamin8000.fare.util.CONSTANTS.STATE_PREFS
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy

class MyApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        try {
            prepareLogger()
            prepareSharedPrefsNames()
        } catch (exception : Exception) {
            Log.d(LOG_TAG, "Application Failed!")
        }
    }
    
    private fun prepareSharedPrefsNames() {
        LICENSE_PREFS = "$packageName.$LICENSE"
        FEEDBACK_PREFS = "$packageName.$FEEDBACK"
        STATE_PREFS = "$packageName.$STATE"
        CITY_PREFS = "$packageName.$CITY"
        GENERAL_PREFS = "$packageName.$GENERAL"
    }
    
    private fun prepareLogger() {
        Logger.addLogAdapter(AndroidLogAdapter(PrettyFormatStrategy.newBuilder().tag(LOG_TAG).build()))
        Logger.d("Application is Started!")
    }
}