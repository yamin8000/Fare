/*
 *     ErrorHelper.kt Created by Yamin Siahmargooei at 2021/7/6
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

package com.github.yamin8000.fare.util.helpers

import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.yamin8000.fare.R
import com.google.android.material.snackbar.Snackbar
import com.orhanobut.logger.Logger

object ErrorHelper {

    fun Fragment.netErrorCache() {
        snack(getString(R.string.net_error_cache_data), Snackbar.LENGTH_INDEFINITE)
    }

    fun Fragment.netError(error: Throwable? = null) {
        snack(getString(R.string.net_error), Snackbar.LENGTH_INDEFINITE)
        if (error != null) Logger.d(error.stackTrace)
    }

    fun Fragment.snack(message: String, length: Int = Snackbar.LENGTH_SHORT): Snackbar? {
        val safeView = this.view
        if (safeView != null) {
            val snackbar = Snackbar.make(safeView, message, length)
            val safeContext = this.context
            if (safeContext != null) {
                snackbar.setTextColor(ContextCompat.getColor(safeContext, R.color.themeBackground))
                snackbar.setBackgroundTint(
                    ContextCompat.getColor(
                        safeContext,
                        R.color.primaryTextColor
                    )
                )
            }
            snackbar.show()
            return snackbar
        } else toast(message)
        return null
    }

    private fun Fragment.toast(message: String, length: Int = Toast.LENGTH_SHORT) {
        val safeContext = this.context
        if (safeContext != null) {
            val toast = Toast.makeText(context, message, length)
            toast.show()
        }
    }
}