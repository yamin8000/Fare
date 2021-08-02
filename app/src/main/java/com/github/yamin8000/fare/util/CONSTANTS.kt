/*
 *     CONSTANTS.kt Created by Yamin Siahmargooei at 2021/7/1
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

package com.github.yamin8000.fare.util

object CONSTANTS {
    
    //general
    const val LOG_TAG = "<--> "
    const val LICENSE = "license"
    const val ROW_LIMIT = 20
    const val CITY = "city"
    const val STATE = "state"
    const val DATE = "date"
    const val CACHE = "cache"
    const val GENERAL = "general"
    const val FUZZY_SEARCH_WINDOW = 3
    const val IS_NIGHT_THEME = "is_night_theme"
    const val CITY_EXTRA = "city_extra"
    const val PRICE_REFERENCE = "price_reference"
    //shared preferences names
    lateinit var GENERAL_PREFS : String
    lateinit var LICENSE_PREFS : String
    lateinit var FEEDBACK_PREFS : String
    lateinit var STATE_PREFS : String
    lateinit var CITY_PREFS : String
    lateinit var CITY_EXTRA_PREFS : String
    lateinit var PRICE_REFERENCE_PREFS : String
    //params
    const val STATE_ID = "state_id"
    const val COUNTY_ID = "county_id"
    const val CITY_ID = "city_id"
    const val ORIGIN = "origin"
    const val DESTINATION = "destination"
    const val LINE_CODE = "code"
    const val FEEDBACK = "feedback"
    const val LIMIT = "limit"
    const val CHOOSING_DEFAULT_CITY = "is_choosing_default_city"
}