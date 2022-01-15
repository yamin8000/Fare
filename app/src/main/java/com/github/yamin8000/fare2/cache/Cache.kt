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

package com.github.yamin8000.fare2.cache

import android.content.Context
import com.github.yamin8000.fare2.util.CONSTANTS.CACHE
import com.github.yamin8000.fare2.util.SharedPrefs
import java.time.LocalDateTime

/**
 * class for caching data in shared preferences
 *
 * @property cacheDatePolicy is a lambda that takes current data and last date that data is cached as input parameter,
 * so you can provide a custom logic to decide whether data is expired or not
 * @see Policy
 *
 * @param context context is used for shared preferences
 * @param sharedPrefsName name of shared preferences file
 */
class Cache(
    context: Context, sharedPrefsName: String,
    private val cacheDatePolicy: Policy = CachePolicy.WeeklyCache,
) {

    val sharedPrefs = SharedPrefs(context, sharedPrefsName)

    /**
     * check if cache data is fresh
     *
     * @return true if cache is new according to class cache policy
     */
    fun isDataFresh(): Boolean {
        val currentDate = LocalDateTime.now()
        val lastDate = sharedPrefs.readDate()
        var isDataExpired = false
        if (lastDate.isNotBlank()) isDataExpired = cacheDatePolicy(currentDate, LocalDateTime.parse(lastDate))
        return lastDate.isNotBlank() && !isDataExpired
    }

    /**
     * Read cache from shared preferences
     *
     * @param key key of shared preference, by default it's "cache" but it can be different
     */
    fun readCache(key: String = CACHE) = sharedPrefs.readString(key)

    /**
     * Write cache to shared preferences
     *
     * @param T type of cache/data
     * @param key key of shared preference, by default it's "cache" but it can be different
     * @param cache data/cache content
     */
    fun <T> writeCache(key: String = CACHE, cache: T) {
        sharedPrefs.writeDate()
        sharedPrefs.write(key, cache)
    }
}