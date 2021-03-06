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

package com.github.yamin8000.fare2.about

import android.os.Bundle
import android.text.Editable
import android.view.View
import com.github.yamin8000.fare2.R
import com.github.yamin8000.fare2.databinding.FragmentFeedbackBinding
import com.github.yamin8000.fare2.model.Feedback
import com.github.yamin8000.fare2.ui.fragment.BaseFragment
import com.github.yamin8000.fare2.util.CONSTANTS.DATE
import com.github.yamin8000.fare2.util.CONSTANTS.FEEDBACK
import com.github.yamin8000.fare2.util.CONSTANTS.FEEDBACK_PREFS
import com.github.yamin8000.fare2.util.SharedPrefs
import com.github.yamin8000.fare2.util.Utility.handleCrash
import com.github.yamin8000.fare2.util.Utility.hideKeyboard
import com.github.yamin8000.fare2.util.helpers.ErrorHelper.netError
import com.github.yamin8000.fare2.util.helpers.ErrorHelper.snack
import com.github.yamin8000.fare2.web.APIs
import com.github.yamin8000.fare2.web.Web
import com.github.yamin8000.fare2.web.Web.asyncResponse
import com.google.android.material.snackbar.Snackbar
import java.time.LocalDateTime

class FeedbackFragment : BaseFragment<FragmentFeedbackBinding>({ FragmentFeedbackBinding.inflate(it) }) {

    private var noticeSnackbar: Snackbar? = null

    private var sharedPrefs: SharedPrefs? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            sharedPrefs = getSharedPrefs()

            val feedbackParam = getFeedbackArgument()
            if (feedbackParam.isNotBlank())
                handleAutomatedReportFromCityLineFragment(feedbackParam)

            binding.sendFeedback.setOnClickListener {
                hideKeyboard()
                createNewFeedback()
            }
        } catch (exception: Exception) {
            handleCrash(exception)
        }
    }

    private fun getFeedbackArgument() = arguments?.getString(FEEDBACK) ?: ""

    private fun getSharedPrefs() = context?.let { SharedPrefs(it, FEEDBACK_PREFS) }

    /**
     * Handle automated report from city line fragment
     *
     * there are two entry to this fragment
     * first => AboutFragment -> FeedbackFragment
     *
     * second => SearchLineFragment -> Toolbar menu button -> FeedbackFragment
     *
     * this method handle the second entry
     *
     */
    private fun handleAutomatedReportFromCityLineFragment(feedbackParam: String) {
        binding.feedbackEdit.setText(feedbackParam)
        binding.feedbackEdit.requestFocus()
        noticeSnackbar = snack(getString(R.string.feedback_notice), Snackbar.LENGTH_INDEFINITE)
    }

    /**
     * Create new feedback
     *
     * handle/decide user data and spam detection
     *
     */
    private fun createNewFeedback() {
        //feedback text, text that describe content of user feedback
        val feedbackText = binding.feedbackEdit.text ?: ""
        //user name, user contact info, can be nullable
        val feedbackUser = binding.feedbackContactEdit.text
        when {
            isSpamming() -> snack(getString(R.string.feedback_spam_notice))
            "$feedbackText".isNotBlank() -> {
                binding.sendFeedback.isEnabled = false
                sendFeedback(feedbackText, feedbackUser)
            }
            else -> snack(getString(R.string.no_feedback_text_entered))
        }
    }

    /**
     * Send actual feedback web request
     *
     * @param feedbackText content of feedback
     * @param feedbackUser user contact info | nullable
     */
    private fun sendFeedback(feedbackText: CharSequence, feedbackUser: Editable?) {
        val feedback = Feedback("$feedbackText", "$feedbackUser")
        val service = Web.getAPI<APIs.FeedbackAPI>()
        service.createFeedback(feedback).asyncResponse(this, { response ->
            /**
             * The HTTP 201 Created success status response code indicates that the request has succeeded
             * and has led to the creation of a resource.
             * The new resource is effectively created before this response is sent back
             * and the new resource is returned in the body of the message,
             * its location being either the URL of the request, or the content of the Location header.
             */
            if (response.code() == 201) handleSuccessfulFeedbackCreationRequest()
            else netError()
            noticeSnackbar?.dismiss()
        }) {
            netError(it)
            noticeSnackbar?.dismiss()
        }
    }

    private fun handleSuccessfulFeedbackCreationRequest() {
        snack(getString(R.string.feedback_created_success))
        resetForm()
        /**
         * write date of last time user created a feedback,
         * this is useful for spam detection
         */
        sharedPrefs?.writeDate()
    }

    /**
     * Reset form to initial state of emptiness
     *
     */
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
    private fun isSpamming(): Boolean {
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