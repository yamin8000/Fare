/*
 *     Fare: find Iran's cities taxi fares
 *     Cache.kt Created by Yamin Siahmargooei at 2021/7/25
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

package com.github.yamin8000.fare.web

import android.content.Context
import com.github.yamin8000.fare.util.CONSTANTS.CACHE
import com.github.yamin8000.fare.util.SharedPrefs
import java.time.LocalDateTime

abstract class Cache(
    context : Context, sharedPrefsName : String,
    private val cacheDatePolicy : (LocalDateTime, LocalDateTime) -> Boolean,
                    ) {
    
    private val sharedPrefs = SharedPrefs(context, sharedPrefsName)
    
    fun isCached() : Boolean {
        val currentDate = LocalDateTime.now()
        val data = sharedPrefs.readString(CACHE)
        val lastDate = sharedPrefs.readDate()
        var isDataExpired = false
        if (lastDate.isNotBlank()) isDataExpired = cacheDatePolicy(currentDate, LocalDateTime.parse(lastDate))
        return data.isNotBlank() && lastDate.isNotBlank() && !isDataExpired
    }
    
    fun readCache() = sharedPrefs.readString(CACHE)
    
    fun <T> writeCache(cache : T) {
        sharedPrefs.writeDate()
        sharedPrefs.write(CACHE, cache)
    }
}