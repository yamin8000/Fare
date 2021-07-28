/*
 *     Utility.kt Created by Yamin Siahmargooei at 2021/7/9
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

import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.yamin8000.fare.R
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.orhanobut.logger.Logger
import java.math.BigInteger

object Utility {
    
    /**
     * Hide keyboard inside fragment
     *
     * since this is not my code and looks shady
     *
     * I don't about any errors that can happen
     *
     * so it's wrapped inside try/catch
     *
     */
    fun Fragment.hideKeyboard() {
        try {
            val activity = this.activity
            if (activity != null) {
                val imm = activity.getSystemService(
                        AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
                var view = activity.currentFocus
                if (view == null) view = View(activity)
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
        } catch (exception : Exception) {
            handleCrash(exception)
        }
    }
    
    /**
     * Handle soft crashes, that are suppressed using try-catch
     *
     * @param exception exception that caught
     */
    fun Fragment.handleCrash(exception : Exception) {
        //navigate user to a special crash screen
        this.findNavController().navigate(R.id.crashFragment)
        //record and send exception data to firebase
        FirebaseCrashlytics.getInstance().recordException(exception)
        //log it logcat
        Logger.d(exception.stackTraceToString())
    }
    
    
    /**
     * Format string number
     *
     * @return number in form of 1,000,000
     */
    fun String?.numFormat() : String {
        if (this == null) return "0"
        return try {
            val number = BigInteger(this)
            String.format("%,d", number)
        } catch (e : Exception) {
            this
        }
    }
}