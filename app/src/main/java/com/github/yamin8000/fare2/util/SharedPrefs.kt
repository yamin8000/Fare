/*
 *     SharedPrefs.kt Created by Yamin Siahmargooei at 2021/7/6
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

package com.github.yamin8000.fare2.util

import android.content.Context
import androidx.core.content.edit
import com.github.yamin8000.fare2.util.CONSTANTS.DATE
import java.time.LocalDateTime

/**
 * ## Shared prefs helper class
 *
 * @param context context
 * @param name shared prefs name
 */
class SharedPrefs(context: Context, name: String) {

    private val sharedPrefs = context.getSharedPreferences(name, Context.MODE_PRIVATE)

    fun readDate() = readString(DATE)

    fun writeDate() = write(DATE, LocalDateTime.now().toString())

    fun <T> write(key: String, value: T) {
        when (value) {
            is String -> writeString(key, value)
            is Boolean -> writeBoolean(key, value)
            is Long -> writeLong(key, value)
            else -> writeString(key, value.toString())
        }
    }

    fun readString(key: String, defaultValue: String = "") = sharedPrefs.getString(key, defaultValue) ?: ""

    private fun writeString(key: String, value: String) = sharedPrefs.edit { putString(key, value) }

    fun readBoolean(key: String, defaultValue: Boolean = false) = sharedPrefs.getBoolean(key, defaultValue)

    private fun writeBoolean(key: String, value: Boolean) = sharedPrefs.edit { putBoolean(key, value) }

    private fun writeLong(key: String, value: Long) = sharedPrefs.edit { putLong(key, value) }

    fun clearData() = sharedPrefs.edit().clear().apply()
}