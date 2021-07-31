/*
 *     FeedbackFragment.kt Created by Yamin Siahmargooei at 2021/7/7
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
import android.text.Editable
import android.view.View
import com.github.yamin8000.fare.R
import com.github.yamin8000.fare.databinding.FragmentFeedbackBinding
import com.github.yamin8000.fare.model.Feedback
import com.github.yamin8000.fare.ui.fragment.BaseFragment
import com.github.yamin8000.fare.util.CONSTANTS.DATE
import com.github.yamin8000.fare.util.CONSTANTS.FEEDBACK
import com.github.yamin8000.fare.util.CONSTANTS.FEEDBACK_PREFS
import com.github.yamin8000.fare.util.SUPABASE.SUPA_BASE_URL
import com.github.yamin8000.fare.util.SharedPrefs
import com.github.yamin8000.fare.util.Utility.handleCrash
import com.github.yamin8000.fare.util.Utility.hideKeyboard
import com.github.yamin8000.fare.util.helpers.ErrorHelper.netError
import com.github.yamin8000.fare.util.helpers.ErrorHelper.snack
import com.github.yamin8000.fare.web.Services
import com.github.yamin8000.fare.web.WEB
import com.github.yamin8000.fare.web.WEB.Companion.asyncResponse
import com.google.android.material.snackbar.Snackbar
import java.time.LocalDateTime

class FeedbackFragment : BaseFragment<FragmentFeedbackBinding>({ FragmentFeedbackBinding.inflate(it) }) {
    
    private var noticeSnackbar : Snackbar? = null
    
    private var sharedPrefs : SharedPrefs? = null
    
    override fun onViewCreated(view : View, savedInstanceState : Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            sharedPrefs = context?.let { SharedPrefs(it, FEEDBACK_PREFS) }
            handleAutomatedReportFromCityLineFragment()
            binding.sendFeedback.setOnClickListener { createNewFeedback() }
        } catch (exception : Exception) {
            handleCrash(exception)
        }
    }
    
    private fun handleAutomatedReportFromCityLineFragment() {
        val feedbackParam = arguments?.getString(FEEDBACK) ?: ""
        if (feedbackParam.isNotEmpty()) {
            binding.feedbackEdit.setText(feedbackParam)
            binding.feedbackEdit.requestFocus()
            noticeSnackbar = snack(getString(R.string.feedback_notice), Snackbar.LENGTH_INDEFINITE)
        }
    }
    
    private fun createNewFeedback() {
        hideKeyboard()
        val feedbackText = binding.feedbackEdit.text ?: ""
        val feedbackUser = binding.feedbackContactEdit.text
        when {
            isSpamming() -> snack(getString(R.string.feedback_spam_notice))
            "$feedbackText".isNotEmpty() -> {
                binding.sendFeedback.isEnabled = false
                sendFeedback(feedbackText, feedbackUser)
            }
            else -> snack(getString(R.string.no_feedback_text_entered))
        }
    }
    
    private fun sendFeedback(feedbackText : CharSequence, feedbackUser : Editable?) {
        val feedback = Feedback("$feedbackText", "$feedbackUser")
        val service = WEB(SUPA_BASE_URL).getService<Services.FeedbackService>()
        service.createFeedback(feedback).asyncResponse(this, {
            if (it.code() == 201) {
                snack(getString(R.string.feedback_created_success))
                resetForm()
                sharedPrefs?.writeDate()
            } else netError()
            noticeSnackbar?.dismiss()
        }) {
            netError()
            noticeSnackbar?.dismiss()
        }
    }
    
    private fun resetForm() {
        binding.feedbackEdit.text?.clear()
        binding.feedbackContactEdit.text?.clear()
        binding.feedbackEdit.requestFocus()
        binding.sendFeedback.isEnabled = true
    }
    
    /**
     * check if user is spamming / sending dummy data using feedback fragment
     *
     * @return true if user is spamming / if trying to send more that one feedback in under 2 minutes
     */
    private fun isSpamming() : Boolean {
        val now = LocalDateTime.now()
        val lastDateString = sharedPrefs?.readString(DATE) ?: ""
        if (lastDateString.isBlank()) return false
        val lastFeedbackDate = LocalDateTime.parse(lastDateString)
        return now.minusMinutes(2).isBefore(lastFeedbackDate)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        noticeSnackbar?.dismiss()
    }
}